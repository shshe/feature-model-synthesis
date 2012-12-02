package gsd.graph

case class DirectedGraph[V] (vs: Set[V], es: EdgeMap[V])
  extends Graph[V](vs,es) with BFS[V] with Cliques[V] {

  type This = DirectedGraph[V]

  def this(vs: Set[V], es: Iterable[Edge[V]]) =
    this(vs, toMultiMap(es) withDefaultValue Set())

  def New(newVs: Set[V], newEs: EdgeMap[V]) =
    new DirectedGraph(newVs,newEs)


  def reverseEdges = New(vs, revEdges)

  def toParseString(implicit toOrdered: V => Ordered[V]) =
    mkParseString("->")

  /**
   * WARNING: Only works on DAGs, will cause an infinite loop on graphs with
   * cycles!
   *
   * For an implication graph g, call:
   *    g.reduceCliques.transitiveReduction.expandCliques
   *
   * to reduce cliques prior to the transitive reduction. Cliques are then
   * expanded out in the reduced graph.
   */
  def transitiveReduction: DirectedGraph[V] = {

    def visit[U](f: (V) => Iterable[V],
                 toVisit: List[V], visited: Set[V] = Set()): Set[V] =
      (toVisit dropWhile { visited contains _ }) match {
        case Nil => visited
        case head::tail => visit(f, tail ::: f(head).toList, visited + head)
      }

    // Transitive successors
    def tsuccessors(v: V): Set[V] = visit(successors, successors(v).toList)

    def doVertex(v : V) =
      for (x <- tsuccessors(v) & (successors(v) flatMap tsuccessors))
      yield ((v, x))

    this -- (vertices flatMap doVertex)
  }

  def isTree: Boolean = {
    val hasOnlyOneParent = es forall { case (_, targets) => targets.size == 1 }
    val isNotAForest = sinks.size <= 1

    hasOnlyOneParent && isNotAForest
  }


}

object DirectedGraph {
  def mkCompleteGraph[V](vs: Set[V]): DirectedGraph[V] =
    new DirectedGraph[V](vs, (vs zip vs).toMap)
}

