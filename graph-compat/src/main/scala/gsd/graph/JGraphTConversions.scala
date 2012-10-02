package gsd.graph

import org.jgrapht.graph.{SimpleGraph, SimpleDirectedGraph, DefaultEdge}

import scala.collection.JavaConversions._

object JGraphTConversions {

  implicit def convertDirectedGraph[T](g: DirectedGraph[T]) = new {
    def toJGraphT: SimpleDirectedGraph[T, DefaultEdge] = {
      val result = new SimpleDirectedGraph[T, DefaultEdge](classOf[DefaultEdge])
      g.vertices.foreach (result.addVertex(_))
      for ((source, targets) <- g.edges; t <- targets)
        result addEdge(source, t)
      result
    }

  }

  implicit def convertUndirectedGraph[T](g: UndirectedGraph[T]) = new {
    def toJGraphT: SimpleGraph[T, DefaultEdge] = {
      val result = new SimpleGraph[T, DefaultEdge](classOf[DefaultEdge])
      g.vertices.foreach(result.addVertex(_))

      for ((src, targets) <- g.edges; target <- targets)
        result.addEdge(src, target)

      result
    }
  }

}
