package gsd.graph

abstract class Graph[V] protected (val vertices: Set[V], val edges: EdgeMap[V])
        extends GraphWriter[V] with Graphviz[V] {

  def this(vs: Set[V], es: Iterable[Edge[V]]) =
    this(vs, toMultiMap(es))

  assert (!(edges exists { case (x,y) => y contains x }),
    "selp-loops not allowed: " + edges)
  
  assert ((edges forall {
    case (x,y) => (vertices contains x) && (y forall (vertices contains))
  }), "Edge contains vertex that is not in this graph!")

  lazy val revEdges: EdgeMap[V] =
    toMultiMap(edges.toStream flatMap { case (src,vs) => vs map ((_, src)) }) withDefaultValue Set()

  type This <: Graph[V]
  def New(newVs : Set[V], newEs: EdgeMap[V]) : This

  def +(t : Edge[V]): This = New(vertices, edges.toEdgeMap addEdge t)
  def -(t : Edge[V]): This = New(vertices, edges.toEdgeMap removeEdge t)

  def ++(ts : Iterable[Edge[V]]): This = New(vertices, edges.toEdgeMap ++ ts)
  def --(ts : Iterable[Edge[V]]): This = New(vertices, edges.toEdgeMap -- ts)

  def successors(v: V): Set[V] = edges(v)
  def predecessors(v: V): Set[V] = revEdges(v)

  def isConnected(v1 : V, v2 : V) = edges(v1).contains(v2)

  def subGraph(vs: Set[V]) = {
    if (!(vs subsetOf vertices))
      System.err.println("Missing vertices in input: " + (vs -- vertices))

    New(vs, toMultiMap {
      asTuples(edges) filter {
        case (s,t) => vs.contains(s) && vs.contains(t)
      }
    })
  }

  /** Vertices with no outgoing edges */
  lazy val sinks = vertices filter { successors(_).isEmpty }

  /** Vertices with no incoming edges */
  lazy val sources = vertices filter { v =>
    !edges.values.exists { _ contains v }
  }

  def toParseString(implicit toOrdered: V => Ordered[V]): String
}

