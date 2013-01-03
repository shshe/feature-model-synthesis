package gsd.fms

import gsd.fms.dnf._
import dk.itu.fms.DNFOrGroups
import java.text.DecimalFormat
import java.io.{FileFilter, File, PrintStream}
import gsd.graph.DirectedGraph

object DNFEvaluation {
  
  sealed abstract class Results(name: String) {
    def isTimeout: Boolean
  }

  // A collection of individual times
  class Times(private val name: String) extends Results(name) {
    private val _times = new collection.mutable.ListBuffer[Long]
    def times = _times.toList
    var totalTime: Long = 0
    def +=(time: Long) =  {
      totalTime += time
      _times += time
    }

    def timeout = new Timeout(name)
    def isTimeout = false
  }

  object Times {
    def unapply(t: Times): Option[(String, List[Long], Long)] = 
      if (t eq null) None
      else Some((t.name, t.times, t.totalTime))
  }
  
  case class Timeout(private val name: String) extends Results(name) {
    def isTimeout = true
  }
  
  def measure(f: => Unit): Long = {
    val begin = System.currentTimeMillis()
    f
    val end = System.currentTimeMillis()
    end - begin
  }

  val formatter = new DecimalFormat("#.###")

  def asSecondsString(time: Long): String =
    formatter.format(time.toFloat / 1000)

  /**
   * @return
   */
  def evaluateGroups(dnf: DNF, 
                     size: Int,
                     implGraph: DirectedGraph[Int],
                     threshold: Long): (Results, Int) = {
    val results = new Times("Groups")
    var numGroups: Int = 0

    // FIXME this is actually very slow at the moment
    // OPTIMIZATION: collapse cliques
    //val collapsedGraph = implGraph
    val collapsedGraph = implGraph.collapseCliques
    val iter = collapsedGraph.vertices.iterator
    while (results.totalTime < threshold && iter.hasNext) {
      val i = iter.next().head // Just take the first element in the clique

      // OPTIMIZATION: retain only relevant features in the formula
      val retained = dnf.retainVars(implGraph.revEdges(i) + i)
      results += measure {
        val groups = DNFOrGroups.orGroups(retained, i)
        // val groups = DNFOrGroups.orGroupsNele(retained, i)
        numGroups += groups.size
      }
    }
    if (results.totalTime > threshold) (results.timeout, -1)
    else (results, numGroups)
  }

  private case class Config(inputFile: Option[String] = None,
                            inputDir: File = new File(getClass.getResource("../../cxt").toURI),
                            timeout: Long = 30000,
                            out: PrintStream = System.out) {
    override def toString: String = {
      val sb = new StringBuilder
      sb append "Input File:     %s\n".format(inputFile)
      sb append "Input Dir:      %s\n".format(inputDir.getCanonicalPath)
      sb append "Timeout:        %s\n".format(timeout.toString)
      sb append "Output:         %s\n".format(out.toString)
      sb.toString()
    }
  }

  /**
   * @param args args(0) is the file to write the output too
   */
  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("DNFEvaluation", "1.0") {
      def options = Seq(
        opt("inputfile", "Input CXT file") {
          (file: String, c: Config) => c.copy(inputFile = Some(file))
        },
        opt("inputdir", "Input directory of CXT files") {
          (dir: String, c: Config) => c.copy(inputDir = new File(dir))
        },
        opt("out", "Output CSV file") {
          (file: String, c: Config) => c.copy(out = new PrintStream(file))
        },
        opt("timeout", "Timeout") {
          (t: String, c: Config) => c.copy(timeout = t.toLong)
        })
    }
    parser.parse(args, Config()) map { c =>
      println(c)

      val files = c.inputFile match {
        case Some(f) => Array(new File(f))
        case None =>
          c.inputDir.listFiles(new FileFilter() {
          def accept(f: File) = f.getName endsWith (".cxt")
        })
      }
      
      c.out.println("name,objects,attributes,time,num,type")
      for (file <- files.sortBy(_.getName)) {
        val name = file.getName
        println("Processing " + name + "...")

        val cxt = CXTParser.parse(file.getCanonicalPath)
        val dnf = cxt.toDNF
        val size = cxt.maxVar
        val threshold = c.timeout

        // Compute Implication Graph
        val builder = new DNFImplBuilder(dnf, size)
        val implTime =  measure {
            builder.implications
          }
        val implGraph = builder.implicationGraph
        val numImpls = builder.implications.size

        // Compute Feature Groups
        val (groupTimes: Results, numGroups) = evaluateGroups(dnf, size, implGraph, threshold)
        if (groupTimes.isTimeout) println("  Timeout!")

        c.out.println("%s,%d,%d,%s,%d,Implications".format(
          name, cxt.objects.size, cxt.attributes.size, asSecondsString(implTime), numImpls))
        c.out.println("%s,%d,%d,%s,%d,Groups".format(
          name, cxt.objects.size, cxt.attributes.size,
        groupTimes match {
          case Times(_, _, totalTime) => asSecondsString(totalTime)
          case Timeout(_) => asSecondsString(-1000)
        }, numGroups))
      }
      c.out.flush()
    } getOrElse {}
  }

}
