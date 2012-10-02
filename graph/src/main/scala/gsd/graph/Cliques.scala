package gsd.graph

trait Cliques[T] {
  this: DirectedGraph[T] with BFS[T] =>

  /**
   * Operates on transitively-closed graphs. Iterates through each vertex V and
   * finds the largest set of bi-connected vertices starting from V by running BFS.
   * Cliques of all sizes are returned (even trivial ones of size 1).
   */
  def findCliques: List[Set[T]] = {
    def _v(rem: List[T], acc: List[Set[T]]): List[Set[T]] = rem match {
      case Nil => acc
      case head::tail =>
        val result = breadthFirstSearch ({
          (curr,next) => successors(next) contains curr
        }, List(head)).toSet

        _v(tail filterNot (result contains), result :: acc)
    }
    _v(vertices.toList, Nil)
  }

  def collapseCliques: DirectedGraph[Set[T]] = {
    val vertices = findCliques
    val vertexMap =
      (for (c <- vertices; v <- c) yield v -> c).toMap withDefault { Set(_) }

    val edges = vertices.flatMap { set =>
      set flatMap { v =>
        ((successors(v) map vertexMap.apply) - set).map { ((set, _)) }
      }
    }
    new DirectedGraph[Set[T]](vertices.toSet, edges)
  }


}

object Cliques {
  implicit def toSetGraph[T](setG : DirectedGraph[Set[T]]) = new {

    /**
     * Returns a DirectedGraph where cliques have been expanded.
     */
    def expandCliques : DirectedGraph[T] = {
      val vs = for {
                  set <- setG.vertices
                  v <- set } yield v

      val es = {
        for { //edges between clique members
          set <- setG.vertices
          v1 <- set
          v2 <- set if v2 != v1
        } yield ((v1, v2))
      } ++
        {
          for { //edges between vertices
            srcSet <- setG.vertices
            tarSet <- setG successors srcSet
            src <- srcSet
            tar <- tarSet
          } yield ((src, tar))
        }

      new DirectedGraph[T](vs, es)
    }
  }


}

