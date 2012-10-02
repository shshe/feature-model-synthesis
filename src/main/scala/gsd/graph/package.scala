package gsd

import graph.Edge

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
  def toMultiMap[T](ts: Iterable[Edge[T]]): EdgeMap[T] = {
    val edgeMap = new collection.mutable.HashMap[T,Set[T]]
    for ((s,t) <- ts)
      edgeMap.put(s, edgeMap.getOrElse(s, Set()) + t)
    edgeMap.toMap
  }

}