package gsd.sat

import org.sat4j.minisat.SolverFactory
import org.sat4j.minisat.orders.PositiveLiteralSelectionStrategy
import gsd.graph.UndirectedGraph

trait MutexBuilder extends SATBuilder with DoneArray {

  override def newSolver = {
    val s = SolverFactory.newMiniLearningHeapRsatExpSimpBiere
    s.getOrder.setPhaseSelectionStrategy(new PositiveLiteralSelectionStrategy)
    s
  }

  lazy val DISPROVER_ATTEMPTS: Int = size
  lazy val DISPROVER_LOTSIZE: Int  = size / 100

  val rand = new scala.util.Random

  /**
   * Returns true iff v1 excludes v2, false otherwise
   */
  def mutex(v1: Int, v2: Int) = !isSatisfiable(List(v1, v2))

  /**
   * TODO Set a time-out.
   *  @param done A (vars + 1) x (vars + 1) array indicating whether the mutex
   *              has been tested.
   */
  def randomDisprover(cutoff: Int,
                      done: Array[Array[Boolean]],
                      lotSize: Int = DISPROVER_LOTSIZE,
                      attempts: Int = DISPROVER_ATTEMPTS) = {

    def mkRandomizedLot: Set[Int] =
      ((0 until lotSize).map { _ => rand.nextInt(cutoff) + 1 }).toSet

    for (i <- 0 until attempts) {
      Console.print("MG: randomized disprover: %d / %d\r".format((i + 1), attempts))
      if (isSatisfiable(mkRandomizedLot))
        for {
          v1 <- 1 to cutoff if solver.model(v1)
          v2 <- 1 to cutoff if solver.model(v2)
        } {
          done(v1)(v2) = true
          done(v2)(v1) = true
        }
    }
    done
  }

  def mkMutexGraph(cutoff: Int,
                   ignore: Iterable[Int]): UndirectedGraph[Int] =
    mkMutexGraph(cutoff, ignore, mkDoneArray(cutoff, ignore))

  def mkMutexGraph(cutoff: Int,
                   ignore: Iterable[Int],
                   done: Array[Array[Boolean]]): UndirectedGraph[Int] = {

    val mutexes = new collection.mutable.ListBuffer[(Int, Int)]

    for {
      i <- 1   to cutoff
      j <- i+1 to cutoff if !done(i)(j)
    } {
      if (mutex(i,j)) {
        mutexes += ((i, j))
        done(i)(j) = true
        done(j)(i) = true
      }
      else {
        // mark non mutexes using the solver model
        for {
          i <- 1 to cutoff
          j <- 1 to cutoff if !done(i)(j) && solver.model(i) && solver.model(j)
        } {
          done(i)(j) = true
          done(j)(i) = true
        }
      }
    }
    new UndirectedGraph[Int]((1 to cutoff).toSet -- ignore, mutexes)
  }
}
