package gsd.graph

/**
 * Constructs a parsable string.  Use GraphParser to read the graph output.
 */
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

    sb.toString()
  }
}
