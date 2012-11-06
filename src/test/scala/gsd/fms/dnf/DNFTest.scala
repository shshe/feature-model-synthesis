package gsd.fms.dnf

import org.scalatest.FunSuite

import gsd.fms.dnf._

class DNFTest extends FunSuite {

  test("max var") {
    expect(4)(Set(Set(1,2,3), Set(4)).maxVar)
    expect(4)(Set(Set(1,2,3), Set(-4)).maxVar)
    expect(7)(Set(Set(1,2,3), Set(-4), Set(7)).negateUnboundedVars(7).maxVar)
  }
  
}
