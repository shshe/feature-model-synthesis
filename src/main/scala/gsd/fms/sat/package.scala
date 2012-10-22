package gsd.fms

package object sat {

  type Clause = Set[Int]
  type CNF = List[Clause]

  implicit def toRichCNF(cnf: CNF) = new {
    def vars: Set[Int] = (cnf.flatten filter (_>0)).toSet
  }
  
}
