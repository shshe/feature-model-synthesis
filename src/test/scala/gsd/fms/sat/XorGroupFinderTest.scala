package gsd.sat

import org.scalatest.FunSuite
import gsd.graph.Tree

class XorGroupFinderTest extends FunSuite {

  test("find xor-group under root") {
    val cnf = List(List(-1, 2, 3), List(-2, -3))
    val hierarchy = new Tree(Set(1,2,3), List(2->1, 3->1))
    val mutexGroups = Set(Set(1,2))
    expect(Set(Set(1,2)))(XorGroupFinder.findXorGroups(cnf, 3, hierarchy, mutexGroups))
  }
  
}
