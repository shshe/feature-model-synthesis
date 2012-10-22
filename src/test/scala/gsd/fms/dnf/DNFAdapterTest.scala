package gsd.fms.dnf

import org.scalatest.FunSuite
import gsd.fms.dnf._
import dk.itu.fms.DNFAdapter
import org.scalatest.matchers.ShouldMatchers

class DNFAdapterTest extends FunSuite with ShouldMatchers {

  test("Simple OR-Groups") {
    val dnf: DNF = Set(Set(1,2), Set(1,3))
    val groups = DNFAdapter.orGroups(dnf,1)
    groups should contain (Set(2,3))
  }

  test("Complex OR-groups") {
    val dnf: DNF = Set(Set(1,2), Set(1,3), Set(1,2,3), Set(1,4))
    val groups = DNFAdapter.orGroups(dnf,1)
    groups should contain(Set(2,3,4))
  }

}
