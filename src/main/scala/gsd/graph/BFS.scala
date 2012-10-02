package gsd.graph

trait BFS[T] {
  this: DirectedGraph[T] =>
  
  def breadthFirstSearch(p: (T,T) => Boolean = { (_:T,_:T) => true },
                         Q: List[T] = sources.toList,
                         closed: Set[T] = Set()): Stream[T] = Q match {
    case Nil => Stream.empty
    case head::tail =>
      Stream.cons(head, breadthFirstSearch(p,
        Q.tail ::: ((successors(head) filter{ p(head,_) }) -- closed).toList,
        closed + head))
  }

}