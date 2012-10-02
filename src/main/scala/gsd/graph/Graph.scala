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

  lazy val revEdges: EdgeMap[V] = toMultiMap {
    edges flatMap {
      case (src,tars) => tars map { ((_, src)) }
    }
  } withDefaultValue Set()

  type This <: Graph[V]
  def New(newVs : Set[V], newEs: EdgeMap[V]) : This

  def +(t : Edge[V]): This = New(vertices, edges.toEdgeMap addEdge t)
  def -(t : Edge[V]): This = New(vertices, edges.toEdgeMap removeEdge t)

  def ++(ts : Iterable[Edge[V]]): This = New(vertices, edges.toEdgeMap ++ ts)
  def --(ts : Iterable[Edge[V]]): This = New(vertices, edges.toEdgeMap -- ts)

  def successors(v: V): Set[V] = edges(v)
  def predecessors(v: V): Set[V] = revEdges(v)

  /** Vertices with no outgoing edges */
  lazy val sinks = vertices filter { successors(_).isEmpty }

  /** Vertices with no incoming edges */
  lazy val sources = vertices filter { v =>
    !edges.values.exists { _ contains v }
  }

  def toParseString(implicit toOrdered: V => Ordered[V]): String
}

case class DirectedGraph[V] protected (vs: Set[V], es: EdgeMap[V])
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

}

trait GraphWriter[V] {
  this: Graph[V] =>

  def mkParseString(edgeSep: String)
                      (implicit toOrdered: V => Ordered[V]): String = {

    val sb = new StringBuilder

    val fmap = Map() ++ (vertices.zipWithIndex map { case (f,i) => (f, i+1) })

    for ((id, v) <- fmap.iterator.toList sortWith { case ((_,i),(_,j)) => i < j })
      sb append v append ": " append id append ";\n"

    var len = 0
    for {
      (src, targets) <- edges.toList sortWith { case ((x,_),(y,_)) => x < y }
      tar <- targets.toList sortWith { _ < _ }
    } {
      val prev = sb.length
      sb append fmap(src) append edgeSep append fmap(tar) append ";"
      val curr = sb.length

      len += curr - prev

      if (len > 80) {
        len = 0
        sb append "\n"
      }
    }
    
    sb toString
  }
  
}

trait Graphviz[T] {
  this: Graph[T] =>

  def toGraphvizString(params: GraphvizParams = GraphvizParams()): String = {
  
    val sb = new StringBuilder

    //Header
    sb append "digraph {\n"
    sb append "graph [ rankdir=%s ];\n".format(params.rankDir)
    sb append "node [ shape=%s ];\n".format(params.shape)

    val fmap = Map() ++ (vertices.zipWithIndex map { case (f,i) => (f, i+1) })

    //Vertices
    for ((id, v) <- fmap.iterator.toList sortWith
            { case ((_,i),(_,j)) => i.toString < j.toString })
      sb append """%d [label="%s"]""".format(v, id.toString replace ("\"", "\\\"")) append "\n"

    for {
      (src, targets) <- edges.toList sortWith { case ((x,_),(y,_)) => x.toString < y.toString }
      tar <- targets.toList sortWith { _.toString < _.toString }
    } {
      sb append fmap(src) append "->" append fmap(tar) append "\n"
    }

    sb append "}"

    sb.toString()
  }
}

case class GraphvizParams(rankDir: String = "TB",
                          shape: String = "box")


