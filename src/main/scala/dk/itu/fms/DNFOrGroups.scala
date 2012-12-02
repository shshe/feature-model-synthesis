package dk.itu.fms

import formula.dnf.DNFClause
import gsd.fms.dnf._

import dk.itu.fms.formula.dnf.{DNF => ITUDNF}

import collection.JavaConversions._
import gsd.graph.DirectedGraph

object DNFOrGroups {
  
  implicit def toITUTerm(term: Term): DNFClause =
    new DNFClause(term.toArray)
  
  implicit def toITUDNF(dnf: DNF): ITUDNF =
    new ITUDNF(dnf map (toITUTerm(_)))

  implicit def toTerm(ituTerm: DNFClause): Term =
    (ituTerm.getLiterals map (_.intValue)).toSet

  def orGroups(dnf: DNF): Set[Set[Int]] =
    ((1 to dnf.maxVar) flatMap 
      (DNFOrGroups.orGroups(dnf, 
        DirectedGraph.mkCompleteGraph[Int]((1 to dnf.maxVar).toSet), _))).toSet

  def orGroups(dnf: DNF, implGraph: DirectedGraph[Int], parent: Int): Set[Set[Int]] = {
    val children = implGraph.revEdges(parent)
    val retained = dnf.retainVars(children + parent)
    println("Retaining: " + (retained))
    (retained.getOrGroups(parent) map (_.map(_.toInt).toSet)).toSet
  }
  
}
