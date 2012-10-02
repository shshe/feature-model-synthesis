package gsd.graph

import org.scalatest.FunSuite

class DirectedGraphTest extends FunSuite {
  
  test("empty graph is a tree") {
    assert(new Tree(Set(), List()).isTree)
  }

  test("tree is a tree") {
    assert(new Tree(Set(1,2,3,4), List(2->1, 3->1, 4->1)).isTree)
  }

  test("graph is not a tree") {
    intercept[IllegalArgumentException]{!(new Tree(Set(1,2,3), List(2->1, 3->1, 3->2)).isTree)}
  }

  test("disconnected node is not a tree") {
    intercept[IllegalArgumentException]{!(new Tree(Set(1,2,3), List(2->1)).isTree)}
  }

}
