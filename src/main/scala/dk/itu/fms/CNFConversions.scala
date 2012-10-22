package dk.itu.fms


import formula.{Clause => ITUClause}
import formula.cnf.{CNFClause, CNF => ITUCNF}
import gsd.fms.sat._

import collection.JavaConversions._

object CNFConversions {

  implicit def toITUCNF(cnf: CNF): ITUCNF =
    new ITUCNF((cnf map toITUClause) : java.util.Collection[CNFClause])

  def toITUClause(clause: Clause): CNFClause =
    new CNFClause(clause.toArray)

  implicit def toClause(clause: ITUClause): Clause =
    (clause.getLiterals map (_.intValue)).toSet
}
