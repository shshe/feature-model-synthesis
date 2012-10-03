package gsd.fms.dnf

import gsd.graph.DirectedGraph

class DNFImplBuilder(val dnf: DNF, val size: Int) {

  lazy val implications: Map[Int, Set[Int]] = {
    val impls = for {
      i <- 1 to size
      j <- 1 to size if j != i &&
             (dnf forall { term => term.contains(-i) && term.contains(j) })
    } yield  (i,j)

    impls groupBy (_._1) mapValues (_ map (_._2) toSet)
  }

  def implication(v1: Int, v2: Int): Boolean =
    implications.get(v1) match {
      case Some(set) => set contains v2
      case None => false
    }

  // TODO create an interface
  def mkImplicationGraph(cutoff: Int = size, ignore: Iterable[Int] = Nil): DirectedGraph[Int] =
    new DirectedGraph[Int]((1 to cutoff).toSet -- ignore, implications)

}
