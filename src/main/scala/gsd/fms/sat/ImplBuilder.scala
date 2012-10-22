package gsd.fms.sat

import gsd.graph._

/**
 * Extension to the SATBuilder enabling the construction of an implication graph.
 *
 * Assumes variables [1 .. cutoff] should have their implications calculated.
 * Variables [cutoff+1 .. m] will be ignored for calculating implications,
 * but are still present in the SAT solver.
 *
 * @author Steven She (shshe@gsd.uwaterloo.ca)
 */
trait ImplBuilder extends SATBuilder with DoneArray {


  /**
   * Returns true iff v1 implies v2, false otherwise
   */
  def implication(v1: Int, v2: Int): Boolean = !isSatisfiable(List(v1, -v2))

  /**
   * Dead features should be removed prior to calling this otherwise these
   * dead features will have implications to all items features!
   *
   * Optimization taken from Nele's implementation: If the formula is
   * satisfiable after a check to implication, then we examine the resulting
   * model. In the model, if there exists i = TRUE, and j = FALSE, then we
   * know that i does NOT imply j.
   *
   * @param ignore any additional variables to ignore
   */
  def mkImplicationGraph(cutoff: Int = size, ignore: Iterable[Int] = Nil): DirectedGraph[Int] = {

    require(cutoff <= size)

    val done: Array[Array[Boolean]] = mkDoneArray(cutoff, ignore)

    // For debugging
    val numTotal = if (debug) {
      var count = 0

      for {
        i <- 1 to cutoff
        j <- 1 to cutoff if !done(i)(j)
      } count += 1

      count
    } else -1

    val result = new collection.mutable.ListBuffer[(Int, Int)]

    for (i <- 1 to cutoff) {
      var numDone = 0

      for (j <- 1 to cutoff if !done(i)(j)) {
        if (implication(i, j)) {
          result += ((i, j))

          done(i)(j) = true
          numDone += 1
        }
        else {
          // mark non-implications using the computed model
          for {
            i <- 1 to cutoff
            j <- 1 to cutoff if solver.model(i) && !solver.model(j) && !done(i)(j)
          } {
            done(i)(j) = true
            numDone += 1
          }
        }
      }
    }

    new DirectedGraph[Int]((1 to cutoff).toSet -- ignore, result)
  }
}
