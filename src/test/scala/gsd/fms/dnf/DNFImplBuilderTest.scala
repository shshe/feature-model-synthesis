package gsd.fms.dnf

import org.scalatest.FunSuite

class DNFImplBuilderTest extends FunSuite {

  test("empty builder is initialized") {
    new DNFImplBuilder(List(), 1)
  }

  test("multiple implications are found") {
    val b = new DNFImplBuilder(
      List(Set(1), Set(1,2), Set(1,2,3)).negateUnboundedVars(3), 3)
    println(b.implications)
    assert(b.implication(2,1))
    assert(b.implication(3,1))
    assert(b.implication(3,2))
    assert(!b.implication(1,2))
  }

}
