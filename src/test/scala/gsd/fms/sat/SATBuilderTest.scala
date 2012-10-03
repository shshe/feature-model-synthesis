package gsd.fms.sat

import org.scalatest.FunSuite
import org.sat4j.specs.ContradictionException

class SATBuilderTest extends FunSuite {

  test("empty builder is initialized") {
    val solver = new SATBuilder(List(), 1)
    assert(solver.isSatisfiable)
  }
  
  test("trivial contradiction should throw a ContradictionException") {
    intercept[ContradictionException]{new SATBuilder(List(List(1), List(-1)), 1)}
  }
  
  test("trivial assumptions") {
    val solver = new SATBuilder(List(List(1,2), List(3,4)), 4)
    assert(solver.isSatisfiable(List()))
    assert(solver.isSatisfiable(List(1,2,3,4)))
    assert(!solver.isSatisfiable(List(-1,-2,-3,-4)))
    assert(!solver.isSatisfiable(List(-1,-2)))
    assert(!solver.isSatisfiable(List(-3,-4)))
  }

}
