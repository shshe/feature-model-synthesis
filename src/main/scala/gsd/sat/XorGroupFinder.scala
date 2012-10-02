package gsd.sat


import gsd.graph.JGraphTConversions._
import collection.JavaConversions._
import org.jgrapht.alg.BronKerboschCliqueFinder
import gsd.graph.{Tree, UndirectedGraph}

class XorGroupFinder(cnf: CNF,
                     size: Int,
                     val mutexGraph: UndirectedGraph[Int]) extends SATBuilder(cnf, size) {

  /**
   * @param mutexGroups assumes groups are consistent with the hierarchy.
   *          Assumes all groups have at least one element.
   *          Assumes group members are all positive integers.
   *
   * @return a set of xor groups where each group contains a set of members.
   */
  def findXorGroups(hierarchy: Tree[Int],
                    mutexGroups: Set[Set[Int]]): Set[Set[Int]] =
    mutexGroups filter { group: Set[Int] =>
      val parents = (group flatMap { member => hierarchy.edges(member) }).toList
      assume(parents.size <= 1)

      // An xor-group is present if the following is unsat:
      //   the parent is present and the members are not
      parents match {
        case parent::Nil =>
          !isSatisfiable((group map (-_)) + parent)
        case Nil =>
          !isSatisfiable(group map (-_))
      }
    }

}

