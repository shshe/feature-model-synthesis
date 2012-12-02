package gsd.fms.dnf

import org.scalatest.FunSuite
import gsd.fms.dnf._
import dk.itu.fms.DNFOrGroups
import org.scalatest.matchers.ShouldMatchers

class DNFAdapterTest extends FunSuite with ShouldMatchers {

  test("Simple OR-Groups") {
    val dnf: DNF = Set(Set(1,2), Set(1,3))
    val groups = DNFOrGroups.orGroups(dnf,1)
    groups should contain (Set(2,3))
  }

  test("Complex OR-Groups") {
    val dnf: DNF = Set(Set(1,2), Set(1,3), Set(1,2,3), Set(1,4))
    val groups = DNFOrGroups.orGroups(dnf,1)
    groups should contain(Set(2,3,4))
  }

  test("More OR-Groups") {
    val dnf: DNF = Set(Set(1,2), Set(1,3), Set(3,4), Set(3,5)).negateUnboundedVars(5)
    DNFOrGroups.orGroups(dnf,1) should be (Set(Set(2,3)))
    DNFOrGroups.orGroups(dnf,2) should be (Set())
    DNFOrGroups.orGroups(dnf,3) should be (Set(Set(1,4,5)))
    DNFOrGroups.orGroups(dnf,4) should be (Set())
    DNFOrGroups.orGroups(dnf,5) should be (Set())
  }

  test("More OR-Groups (2)") {
    val dnf: DNF = Set(Set(3,4), Set(3,5)).negateUnboundedVars(5)
    DNFOrGroups.orGroups(dnf,1) should be (Set())
    DNFOrGroups.orGroups(dnf,2) should be (Set())
    DNFOrGroups.orGroups(dnf,3) should be (Set(Set(4,5)))
    DNFOrGroups.orGroups(dnf,4) should be (Set())
    DNFOrGroups.orGroups(dnf,5) should be (Set())
  }

  test("OR-Groups with hierarchy") {
    val dnf: DNF = Set(Set(1,2),
                       Set(1,3,4),
                       Set(1,2,3,4),
                       Set(1,2,3,5),
                       Set(1,3,4,5),
                       Set(1,2,3,4,5)).negateUnboundedVars(5)

    DNFOrGroups.orGroups(dnf,1, Set(2,3)) should be (Set(Set(2,3)))
    DNFOrGroups.orGroups(dnf,2, Set()) should be (Set())
    DNFOrGroups.orGroups(dnf,3, Set(4,5)) should be (Set(Set(4,5)))
    DNFOrGroups.orGroups(dnf,4, Set()) should be (Set())
    DNFOrGroups.orGroups(dnf,5, Set()) should be (Set())
  }
  
  test("OR-Groups with hierarchy (2)") {
    val dnf: DNF = Set(
      Set(1, 3),
      Set(1, 2, 3),
      Set(1, 2, 4),
      Set(1, 3, 4),
      Set(1, 2, 3, 4)).negateUnboundedVars(5)
    DNFOrGroups.orGroups(dnf,1, Set(2,3,4)) should be (Set(Set(3,4), Set(2,3)))
  }
  
}
