package gsd.fms.modelcompare

import java.io.File
import de.tud.iai.modelcompare.splot.SxfmContextCreator
import colibri.lib.{ComparableSet, Relation}

import gsd.fms.dnf._
import de.tud.iai.modelcompare.fca.ManyObjectsFCAUtil

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

  def toDNF(context: Relation): (DNF, Map[Comparable[_], Int]) = {
    import collection.JavaConversions._

    val attrMap =
      (context.getAllAttributes zip (1 to context.getSizeAttributes)).toMap

    val terms = for (obj <- context.getAllObjects) yield {
      val posAttributes = context.getAttributeSet(obj)
      val negAttributes = (attrMap.keySet -- posAttributes)
      val posLiterals = (posAttributes map attrMap).toSet
      val negLiterals = (negAttributes map attrMap map (-_)).toSet

      posLiterals ++ negLiterals
    }

    (terms.toSet, attrMap)
  }



  def main(args: Array[String]) {
    val file = new File("sxfm/REAL-FM-5.xml") // Thread-Domain
    //val file = new File("sxfm/REAL-FM-20.xml") // Thread-Domain
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

    val (lattice, latticeTime) = nanoTime {
      val l = contextCreator.createLattice(context)
      new ManyObjectsFCAUtil(l); // creates also new attribute cache
      l
    }
    println("Time to compute attribute concepts (for dead and mandatory features): " +
      inMs(latticeTime) + "ms")

    val (fcaUtil, latticeTime2) = nanoTime(new ManyObjectsFCAUtil(lattice)) // recreates fcalib to avoid measuring classloader runtime
    println("Time to compute attribute concepts (for dead and mandatory features without class loader): " +
      inMs(latticeTime2) + "ms")

    println()
    println("---")
    println()

    //
    // Convert context to DNF
    //
    val (dnf, attrMap) = toDNF(context)

    println("Number of literals (DNF): " + dnf.maxVar)
    println("Number of terms (DNF): " + dnf.size)

    //
    // Compute implication graph
    //
    val builder = new DNFImplBuilder(dnf, dnf.size)
    val (numImpls, implTime) =  nanoTime(builder.implications.size)

    println("Time to compute implication graph: " + inMs(implTime) + "ms")

  }

}
