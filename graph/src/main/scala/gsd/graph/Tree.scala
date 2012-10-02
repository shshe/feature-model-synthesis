package gsd.graph

class Tree[V](vs: Set[V], es: Iterable[Edge[V]]) extends DirectedGraph[V](vs, es) {
  require(this.isTree)

  lazy val parentChildMap = revEdges

  lazy val siblingSets: Set[Set[V]] = revEdges.values.toSet

}
