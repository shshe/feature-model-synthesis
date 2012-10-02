package gsd.dnf

import org.scalatest.FunSuite

class DNFImplBuilderTest extends FunSuite {

  test ("empty builder is initialized") {
    new DNFImplBuilder(List(),1)
  }

  test ("implication is found") {
    val b1 = new DNFImplBuilder(List(List(-1,2)),2)
    assert(b1.implication(1,2))

    val b2 = new DNFImplBuilder(List(List(-1,2), List(-1,2,3)),3)
    assert(b2.implication(1,2))
  }

  test ("invalid implication is not found") {
    val b = new DNFImplBuilder(List(List(-1,2), List(2)), 2)
    assert(!b.implication(1,2))
  }

}
