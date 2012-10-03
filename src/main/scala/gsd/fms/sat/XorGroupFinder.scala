package gsd.fms.sat

import gsd.graph.{Tree, UndirectedGraph}

object XorGroupFinder {

  /**
   * @param mutexGroups assumes groups are consistent with the hierarchy.
   *          Assumes all groups have at least one element.
   *          Assumes group members are all positive integers.
   *
   * @return a set of xor groups where each group contains a set of members.
   */
  def findXorGroups(cnf: CNF,
                    size: Int,
                    hierarchy: Tree[Int],
                    mutexGroups: Set[Set[Int]]): Set[Set[Int]] = {
    val solver = new SATBuilder(cnf, size)

    mutexGroups filter { group: Set[Int] =>
      val parents = (group flatMap { member => hierarchy.edges(member) }).toList
      assume(parents.size <= 1)

      // An xor-group is present if the following is unsat:
      //   the parent is present and the members are not
      parents match {
        case parent::Nil =>
          !solver.isSatisfiable((group map (-_)) + parent)
        case Nil =>
          !solver.isSatisfiable(group map (-_))
      }
    }
  }

}

