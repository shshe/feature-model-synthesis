package gsd.fms

import gsd.fms.dnf._
import dk.itu.fms.DNFOrGroups
import java.text.DecimalFormat
import java.io.{FileFilter, File, PrintStream}
import gsd.graph.DirectedGraph

object DNFEvaluation {

  def measure(f: => Unit): Long = {
    val begin = System.currentTimeMillis()
    f
    val end = System.currentTimeMillis()
    end - begin
  }

  val formatter = new DecimalFormat("#.###")

  def asSecondsString(time: Long): String =
    formatter.format(time.toFloat / 1000)

  def evaluateGroups(dnf: DNF, size: Int, implGraph: DirectedGraph[Int], threshold: Long): (Long, Int) = {
    var time: Long = 0
    var numGroups: Int = 0
    var i = 1
    while (time < threshold && i <= dnf.maxVar) {
      time += measure {
        val groups = DNFOrGroups.orGroups(dnf, implGraph, i)
        numGroups += groups.size
      }
      i += 1
    }
    if (time > threshold) (-1000, -1)
    else (time, numGroups)
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
        val (groupTime, numGroups) = evaluateGroups(dnf, size, implGraph, threshold)
        if (groupTime < 0) println("  Timeout!")

        c.out.println("%s,%d,%d,%s,%d,Implications".format(
          name, cxt.objects.size, cxt.attributes.size, asSecondsString(implTime), numImpls))
        c.out.println("%s,%d,%d,%s,%d,Groups".format(
          name, cxt.objects.size, cxt.attributes.size, asSecondsString(groupTime), numGroups))
      }
      c.out.flush()
    } getOrElse {}
  }

}
