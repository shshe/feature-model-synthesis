package gsd.fms.sat

import org.sat4j.minisat.SolverFactory
import org.sat4j.core.VecInt
import org.sat4j.specs.{ContradictionException, IConstr, ISolver}


/**
 * WARNING: The SAT solver has its own internal state, be careful about
 * calling certain stateful operations (i.e. like model) on the SAT solver.
 *
 * @author Steven She (shshe@gsd.uwaterloo.ca)
 *
 * @param size the max variable in the CNF formula such that variables
 *             [1 .. size] are variables in the SAT solver.
 * @throws ContradictionException if a trivial contradiction is detected
 *             when the solver is initialized.
 */
@throws(classOf[ContradictionException])
class SATBuilder(val cnf: CNF, val size: Int) {

  import SATBuilder._

  var debug = false
  val solver: ISolver = init

  /**
   * Can be overridden by a subclass to initialize a specialized solver.
   */
  protected def newSolver: ISolver = {
    val s = SolverFactory.newDefault()
    s newVar size
    s
  }

  private def init: ISolver = {
    val s = newSolver

    def addClause(clause: Clause) {
      assert(!clause.contains(0), "Clause cannot contain 0")
      val vi = toVecInt(clause)
      s.addClause(vi)
      vi.clear()
    }

    try {
      //FIXME workaround for free variables not appearing in models
      for (i <- 1 to size) addClause(Set(i, -i))
      cnf foreach addClause
    }
    catch {
      case e: ContradictionException => throw e
    }
    s
  }

  def isSatisfiable =
    solver.isSatisfiable

  def isSatisfiable(assump: Iterable[Int]) =
    solver.isSatisfiable(toVecInt(assump))

}

object SATBuilder {

  /**
   * Utility function to convert an interable of Ints to a VecInt used
   * by SAT4J.
   */
  def toVecInt(lits: Iterable[Int]): VecInt =
    new VecInt(lits.toArray)

}
