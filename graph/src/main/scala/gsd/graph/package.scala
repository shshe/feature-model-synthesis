package gsd

package object graph {
  
  type Edge[+T] = (T, T)
  type EdgeMap[T] = Map[T,Set[T]]

  implicit def toRichEdgeMap[T](map : Map[T,Set[T]]) =
    new RichEdgeMap[T](map)

  class RichEdgeMap[T](map: Map[T,Set[T]]) {

    def addEdge(t: Edge[T]): EdgeMap[T] = t match {
      case (u,v) => map.get(u) match {
        case Some(vs) => map - u + Tuple2(u, vs + v)
        case None => map + Tuple2(u, Set(v))
      }
    }

    def removeEdge(t: Edge[T]): EdgeMap[T] = t match {
      case (u,v) => map.get(u) match {
        case Some(vs) if vs == Set(v) => map - u
        case Some(vs) if vs == Set() => map
        case Some(vs) => map - u + ((u, vs - v))
        case None  => map
      }
    }

    def --(ts : Iterable[Edge[T]]): EdgeMap[T] =
      ts.foldLeft(map)((x,y) => x addEdge y)

    def ++(ts : Iterable[Edge[T]]): EdgeMap[T] =
      ts.foldLeft(map)((x,y) => x removeEdge y)

    /**
     * Strict conversion to RichEdgeMap
     */
    def toEdgeMap = this
    
  }

  /**
   * Creates a multi map from a collection of edges.
   * Used to create a directed graph.
   */
  def toMultiMap[T](ts: Iterable[Edge[T]]): EdgeMap[T] =
    ts groupBy (_._1) mapValues (_ map  (_._2) toSet)

  /**
   * Orders an edge Edge(x,y) such that x < y and calls toMultiMap on it.
   */
  def toUndirectedMultiMap[T <% Ordered[T]](ts: Iterable[Edge[T]]): EdgeMap[T] =
    toMultiMap((ts map {
      case (x,y) if x < y => (x,y)
      case (x,y) => (y,x)
    }).toSet)

  def asTuples[T](edges : Map[T,Set[T]]) =
    edges.flatMap {
      case (src,tars) => tars.map {
        t => (src, t)
      }
    }

}