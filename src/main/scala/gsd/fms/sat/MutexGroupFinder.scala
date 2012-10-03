package gsd.fms.sat

import gsd.graph.UndirectedGraph
import gsd.graph.JGraphTConversions._
import collection.JavaConversions._
import org.jgrapht.alg.BronKerboschCliqueFinder

object MutexGroupFinder {
  def findMaximalCliques[T](g: UndirectedGraph[T]): List[Set[T]] = {
    val finder = new BronKerboschCliqueFinder(g.toJGraphT)
    finder.getAllMaximalCliques.toList.map(_.toSet).filter(_.size > 1)
  }

  /**
   * @return a set of mutex groups where each group contains a set of members
   */
  def findMutexGroups(mutexGraph: UndirectedGraph[Int]): Set[Set[Int]] =
    findMaximalCliques(mutexGraph).toSet

  def findMutexGroups(mutexGraph: UndirectedGraph[Int],
                      siblingGroups: Set[Set[Int]]): Set[Set[Int]] =
    (siblingGroups flatMap { siblings =>
      findMaximalCliques(mutexGraph.subGraph(siblings))
    })
}

