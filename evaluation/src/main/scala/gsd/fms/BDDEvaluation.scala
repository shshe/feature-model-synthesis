package gsd.fms

import java.io.{FileFilter, File, PrintStream}
import net.sf.javabdd.{BDDFactory, BDD}
import gsd.graph.DirectedGraph

object BDDEvaluation {

  /**
   * @return time in nanoseconds
   */
  def nanoTime[T](f: => T): (T, Long) = {
    val t_ = System.nanoTime()
    val result = f
    val t__ = System.nanoTime()
    (result, t__ - t_)
  }

  def inMs(time: Long): Long =
    time / 1000000

  def process(name: String, bdd: BDD, B: BDDFactory, out: PrintStream) {

    // Compute implication graph
    val s: BDD = bdd.support
    val isup = s.scanSet
    val (min_var, max_var) =
      if (isup != null && isup.length > 0)
        (Util.min (isup), Util.max(isup))
      else (0, 0)

    val (implications, implicationsTime) = nanoTime {

      val implications = new collection.mutable.ListBuffer[(Int, Int)]
      for (i <- min_var to max_var) {
        val temp = bdd.id ().andWith (B.nithVar(i))
        val falsified = if (temp.isZero) {
          // if temp is true in all assignments then it is implied by all other variables
          ((min_var to max_var).toSet - i)
        }
        else {
          val vd = new ValidDomains (bdd, min_var, max_var)
          (min_var to max_var) filter { j =>
            vd.canBeZero(j) && !vd.canBeOne(j)
          }
        }

        temp.free()
        implications ++= falsified map (_ -> i)

      }
      implications
    }

    println("Time to compute implications: " + inMs(implicationsTime) + "ms")

    val implg = new DirectedGraph[Int](
      (min_var to max_var).toSet,
      implications  groupBy (_._1) mapValues
        (_ map (_._2) toSet) withDefault (_ => Set.empty[Int]))


    println()
    println("Collapsing cliques...")
    val cliqueg: DirectedGraph[Set[Int]] = implg.collapseCliques


    println()
    println("Computing feature groups...")
    val b = (min_var to max_var) map
      bdd.getFactory.nithVar reduceLeft (_.andWith(_))

    val (groups, groupsTime) = nanoTime {
      val groups = new collection.mutable.ListBuffer[(Int, Set[java.lang.Integer])]
      for (clique <- cliqueg.vertices) {
        val i = clique.head
        val descendants = cliqueg.revEdges(clique).map(_.head)
        val remove = (min_var to max_var).toSet -- descendants - i

        val exist = bdd.getFactory.makeSet(remove.toArray)
        val fragment = bdd.exist(exist)

        val bid = b.id()
        val pi = new PrimeImplicants(bid, fragment)

        bid.free()
        fragment.free()
        exist.free()

        import collection.JavaConversions._
        val g = pi.toSet[Implicant] map
          (_.removeNegations()) filter
          (p => p.size() >= 2 && !p.contains(i))
        groups ++= g map (i -> _.toSet)
      }
      groups
    }

    b.free()


    // FIXME number of features

    val numConfigs = bdd.satCount().toLong

    out.println("%s,%d,%d,%s,%d,Implications".format(
      name, numConfigs, max_var, inMs(implicationsTime), implications.size))
    out.println("%s,%d,%d,%s,%d,Groups".format(
      name, numConfigs, max_var, inMs(groupsTime), groups.size))

  }





  private case class Config(input: File = new File(getClass.getResource("../../splot-bdd").toURI),
                            out: PrintStream = System.out)

  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("BDDEvaluation", "1.0") {
      def options = Seq(
        opt("f", "input", "Input file or directory containing .bdd files") {
          (file: String, c: Config) => c.copy(input = new File(file))
        },
        opt("out", "Output CSV file") {
          (file: String, c: Config) => c.copy(out = new PrintStream(file))
        }
      )
    }
    parser.parse(args, Config()) map { c =>
      val files = if (c.input.isFile) Array(c.input)
      else c.input.listFiles(new FileFilter() {
        def accept(f: File) = f.getName endsWith ".bdd"
      })

      // HACK TO PREVENT null pointer exception on the LEVEL method
      var i = 300
      for (f <- files) {
        val factory = BDDFactory.init("java", 100000, 100000)
        factory.setVarNum(i)
        i += 1
        println("Working on: " + f.getName)
        val bdd = factory.load(f.getCanonicalPath.toString)

        process(f.getName, bdd, factory, c.out)

        bdd.free()
        factory.reset()
      }





    } getOrElse {
    }
  }
}
