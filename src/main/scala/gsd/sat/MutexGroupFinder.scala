package gsd.sat

import gsd.graph.UndirectedGraph
import org.jgrapht.alg.BronKerboschCliqueFinder

class MutexGroupFinder {

}

object MutexGroupFinder {
  def findMaximalCliques[T](g: UndirectedGraph[T]): List[Set[T]] = {
    val finder = new BronKerboschCliqueFinder(g.toJGraphT)
    finder.getAllMaximalCliques.toList.map(_.toSet).filter(_.size > 1)
  }
}

