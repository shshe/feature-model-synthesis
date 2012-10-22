package gsd.fms.sat

object CNFBuilder {

  implicit def toClause(in: List[Int]): Clause = in.toSet

  def mkCNF(clauses: Clause*): CNF =
    clauses.toList

}
