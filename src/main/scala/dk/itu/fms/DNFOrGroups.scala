package dk.itu.fms

import formula.Clause
import formula.dnf.{DefaultDNFSolver, DNFClause, DNF => ITUDNF}
import gsd.fms.dnf._

import collection.JavaConversions._
import prime.Prime

object DNFOrGroups {
  
  implicit def toITUTerm(term: Term): DNFClause =
    new DNFClause(term.toArray)
  
  implicit def toITUDNF(dnf: DNF): ITUDNF =
    new ITUDNF(dnf map (toITUTerm(_)))

  implicit def toTerm(ituTerm: DNFClause): Term =
    (ituTerm.getLiterals map (_.intValue)).toList

  implicit def toScalaSet(in: Clause): Set[Int] =
    (in map (_.toInt)).toSet

  // Translated from Nele's DNF
  def orGroups(dnf: DNF,  parent: Int): Set[Set[Int]] = {
    // Returns Java sets
    val primes = new Prime(new DefaultDNFSolver(dnf, parent)).positivePrimes
    (primes filter
      (prime => prime.size > 1 && (prime forall (_ > 0))) map
      (prime => (prime map (_.intValue)).toSet)).toSet
  }
  
  def orGroupsNele(dnf: DNF, parent: Int) =
    dnf.getOrGroups(parent)
  
}
