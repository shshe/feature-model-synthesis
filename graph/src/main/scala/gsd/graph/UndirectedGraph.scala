package gsd.graph

case class UndirectedGraph[V <% Ordered[V]] protected (vs: Set[V], es: EdgeMap[V])
  extends Graph[V](vs, es) {

  type This = UndirectedGraph[V]

  def this(vs: Set[V], es: Iterable[Edge[V]]) =
    this(vs, toUndirectedMultiMap(es))

  def New(newVs: Set[V], newEs: EdgeMap[V]) =
    new UndirectedGraph(newVs,newEs)

  def toParseString(implicit toOrdered: V => Ordered[V]) =
    mkParseString("--")(toOrdered)
}

