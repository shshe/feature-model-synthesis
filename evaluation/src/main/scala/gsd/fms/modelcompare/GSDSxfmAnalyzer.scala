package gsd.fms.modelcompare

import java.io.File
import de.tud.iai.modelcompare.splot.SxfmContextCreator
import colibri.lib.{ComparableSet, Relation}

import gsd.fms.dnf._
import de.tud.iai.modelcompare.fca.ManyObjectsFCAUtil
import dk.itu.fms.formula.dnf.{DNFClause, DefaultDNFSolver, DNF => ITUDNF}
import dk.itu.fms.prime.Prime
import gsd.graph.DirectedGraph
import gsd.fms.ImplicationGraph

object GSDSxfmAnalyzer {

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

  def toITUDNF(context: Relation): (ITUDNF, Map[Comparable[_], Int]) = {
    import collection.JavaConversions._

    val attrMap =
      (context.getAllAttributes zip (1 to context.getSizeAttributes)).toMap

    val terms = for (obj <- context.getAllObjects) yield {
      val posAttributes = context.getAttributeSet(obj)
      val negAttributes = (attrMap.keySet -- posAttributes)
      val posLiterals = (posAttributes map attrMap).toSet
      val negLiterals = (negAttributes map attrMap map (-_)).toSet

      new DNFClause((posLiterals ++ negLiterals).toArray)
    }

    (new ITUDNF(terms), attrMap)
  }


  def main(args: Array[String]) {
    //val file = new File("sxfm/REAL-FM-5.xml") // Thread-Domain
    val file = new File("sxfm/REAL-FM-20.xml") // Thread-Domain
    val contextCreator = new SxfmContextCreator(SxfmContextCreator.OutputType.NAME_ONLY)

    //
    // Load file
    //
    println("Load " + file.toString + "...")
    if (!contextCreator.loadFile(file)) sys.error("File load failed!")

    //
    // Generate all configurations as a context
    //
    val (context, contextTime) = nanoTime(contextCreator.createContext())

    println("Time to compute all configurations: " + inMs(contextTime) + "ms")
    println("Number of features (Context): " + context.getSizeAttributes)
    println("Number of configurations (Context): " + context.getSizeObjects)

    println()

//    val (lattice, latticeTime) = nanoTime {
//      val l = contextCreator.createLattice(context)
//      new ManyObjectsFCAUtil(l); // creates also new attribute cache
//      l
//    }
//    println("Time to compute attribute concepts (for dead and mandatory features): " +
//      inMs(latticeTime) + "ms")
//
//    val (fcaUtil, latticeTime2) = nanoTime(new ManyObjectsFCAUtil(lattice)) // recreates fcalib to avoid measuring classloader runtime
//    println("Time to compute attribute concepts (for dead and mandatory features without class loader): " +
//      inMs(latticeTime2) + "ms")

    println()
    println("---")
    println()


    println("Converting to ITU DNF representation")
    val ((ituDNF, attrMap), ituDNFTime) = nanoTime(toITUDNF(context))
    println("Time to convert to ITU DNF representation: " + inMs(ituDNFTime) + "ms")
    println("Number of literals (DNF): " + ituDNF.getNumberOfVariables)
    println("Number of terms (DNF): " + ituDNF.size)

    println()
    println("Computing implication graph")

    // val implBuilder = new ImplicationGraph(ituDNF)
    // val (implications, implicationsTime) = nanoTime(implBuilder.calcImplications() map {
    //   case Array(i,j) => i.toInt -> j.toInt
    // })

    val (implications, implicationsTime) = nanoTime {
      val results = new collection.mutable.ListBuffer[(Int, Int)]
      for (i <- ituDNF.getVariables) {
        for (j <- ituDNF.getVariables if i != j) {
          if (ituDNF.implication(i, j)) results += i -> j
        }
      }
      results
    }

    println()
    println("Time to compute implications: " + inMs(implicationsTime) + "ms")
    println("Number of implications: " + implications.size)

    val implg = new DirectedGraph[Int](ituDNF.getVariables.toSet,
        implications  groupBy (_._1) mapValues
          (_ map (_._2) toSet) withDefault (_ => Set.empty[Int]))

    println("Number of vertices (variables): " + implg.vertices.size)



    //
    // Compute OR-Groups
    // NOTE: AND-groups are not collapsed in the graph

    var totalGroupTime = 0L
    for (i <- implg.vertices) {
      //
      // Retain only descendants in the formula
      //
      // NOTE: This takes a long time
      // NOTE: time for variable elimination is not included in total group time
      val retainVars = implg.revEdges(i) + i
      val eliminateVars = implg.vertices -- retainVars

      import collection.JavaConversions._
      println("Calculating feature groups for " + i)
      // Returns Java sets
      val (primes, groupTime) = nanoTime{
        new Prime(
          new DefaultDNFSolver(
            ituDNF,
            i,
            eliminateVars map (new java.lang.Integer(_)))).positivePrimes
      }
      totalGroupTime += groupTime

      // FIXME: is this needed
      // (primes filter
      //   (prime => prime.size > 1 && (prime forall (_ > 0))) map
      //   (prime => (prime map (_.intValue)).toSet)).toSet
    }

    println()
    println("Time to compute groups: " + inMs(totalGroupTime) + "ms")
 }

}
