package gsd.fms.sat

import org.scalatest.FunSuite
import gsd.graph.UndirectedGraph

class MutexGroupFinderTest extends FunSuite {

  test("find maximal cliques (1)") {
    val cliques = MutexGroupFinder.findMaximalCliques(
      new UndirectedGraph[Int](Set(1,2), List(1->2)))
    expect(Set(Set(1,2)))(cliques.toSet)
  }

  test("find maximal cliques (2)") {
    val cliques = MutexGroupFinder.findMaximalCliques(
      new UndirectedGraph[Int](Set(1,2,3,4), List(1->2, 3->4)))
    expect(Set(Set(1,2), Set(3,4)))(cliques.toSet)
  }

  test("find maximal cliques (3)") {
    val cliques = MutexGroupFinder.findMaximalCliques(
      new UndirectedGraph[Int](Set(1,2,3,4), List(1->2, 2->3, 3->4)))
    expect(Set(Set(1,2), Set(2,3), Set(3,4)))(cliques.toSet)
  }

  test("find maximal cliques (4)") {
    val cliques = MutexGroupFinder.findMaximalCliques(
      new UndirectedGraph[Int](Set(1,2,3), List(1->2, 2->3, 1->3)))
    expect(Set(Set(1,2,3)))(cliques.toSet)
  }
  
  test("find trivial mutex group") {
    val mutexGraph = new UndirectedGraph[Int](Set(1,2), List(1->2))
    expect(Set(Set(1,2)))(MutexGroupFinder.findMutexGroups(mutexGraph))
  }

  test("find trivial mutex group with sibling groups") {
    val mutexGraph = new UndirectedGraph[Int](Set(1,2,3,4), List(1->2, 3->4))
    expect(Set(Set(1,2)))(MutexGroupFinder.findMutexGroups(mutexGraph, Set(Set(1,2))))
  }

}
