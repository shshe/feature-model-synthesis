package gsd.sat

import org.scalatest.FunSuite
import gsd.graph.DirectedGraph

class ImplBuilderTest extends FunSuite {
  
  test("empty builder is initialized") {
    new ImplBuilder(List(),1)
  }

  test("implication is found") {
    val b = new ImplBuilder(List(List(-1,2)), 2)
    assert(b.implication(1,2))
  }
  
  test("invalid implication is not found") {
    val b = new ImplBuilder(List(List(1,2)), 2)
    assert(!b.implication(1,2))
  }

  test("transitive implications are found") {
    val b = new ImplBuilder(List(List(-1,2), List(-2,1), List(-2,3)), 3)
    assert(b.implication(1,2))
    assert(b.implication(2,3))
    assert(b.implication(1,3))
    assert(!b.implication(3,2))
    assert(!b.implication(3,1))
  }

  test("mkImplicationGraph - cutoff larger than size should throw an exception") {
    val b = new ImplBuilder(List(),1)
    intercept[IllegalArgumentException]{
      b.mkImplicationGraph(2)
    }
  }
  
  test("mkImplicationGraph - implications are found") {
    val b = new ImplBuilder(List(List(-1,2), List(-2,3), List(-3,1)),3)
    expect(new DirectedGraph[Int](
      Set(1,2,3), List((1,2),(1,3), (2,1), (2,3), (3,1), (3,2))))(
        b.mkImplicationGraph())
  }

  test("mkImplicationGraph - cutoff should ignore variables in the resulting graph") {
    val b = new ImplBuilder(List(List(-1,2), List(-2,3), List(-3,1)),3)
    expect(new DirectedGraph[Int](Set(1,2), List((1,2),(2,1))))(b.mkImplicationGraph(2))
  }

  test("mkImplicationGraph - ignored variables should not appear in the resulting graph") {
    val b = new ImplBuilder(List(List(-1,2), List(-2,3), List(-3,1)),3)
    expect(new DirectedGraph[Int](Set(2,3), List((2,3),(3,2))))(b.mkImplicationGraph(3, List(1)))
  }

}
