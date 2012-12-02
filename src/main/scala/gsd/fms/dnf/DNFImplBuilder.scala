package gsd.fms.dnf

import gsd.graph.DirectedGraph

class DNFImplBuilder(val dnf: DNF, val size: Int) {

  // false if there exists a term that contains i && !j
  // true if forall terms !i || j
  lazy val implications: Seq[(Int, Int)] = 
    for {
        i <- 1 to size
        j <- 1 to size if i != j && implication(i, j)
      } yield (i,j)

  def implication(i: Int, j: Int): Boolean =
    dnf forall { term => term.contains(-i) || term.contains(j) }

  lazy val implicationGraph: DirectedGraph[Int] =
  new DirectedGraph[Int]( (1 to size).toSet,
    implications groupBy (_._1) mapValues
      (_ map (_._2) toSet) withDefault (_ => Set.empty[Int]))

}

