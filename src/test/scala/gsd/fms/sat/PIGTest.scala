package gsd.fms.sat

import org.scalatest.FunSuite

import dk.itu.fms.{CNFConversions, PIGAdapter}
import CNFConversions._
import org.scalatest.matchers.ShouldMatchers


class PIGTest extends FunSuite with ShouldMatchers {
  
  test("OR-Groups") {
    val cnf = List(Set(-2,1),Set(-3,1),Set(-1,2,3))
    PIGAdapter.orGroups(cnf, 1, Set(2,3)) should contain (Set(2,3))
    PIGAdapter.orGroups(cnf, 1) should contain (Set(2,3))
  }

}
