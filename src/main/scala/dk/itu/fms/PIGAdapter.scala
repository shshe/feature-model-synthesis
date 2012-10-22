package dk.itu.fms

import gsd.fms.sat._

import CNFConversions._
import collection.JavaConversions._

object PIGAdapter {

  def orGroups(cnf: CNF, parent: Int, members: Set[Int]): Set[Set[Int]] = {
    val eliminate = cnf.vars -- members - parent
    val set = setAsJavaSet(eliminate map (x => x :java.lang.Integer))
    (cnf.eliminateVariables(set).getOrGroups(parent) map {
      s => s map (_.toInt)
    }).toSet
  }
  
  def orGroups(cnf: CNF, parent: Int): Set[Set[Int]] =
    (cnf.getOrGroups(parent) map (_.map(_.toInt))).toSet

}
