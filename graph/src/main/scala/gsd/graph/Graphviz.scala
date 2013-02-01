package gsd.graph

trait Graphviz[T] {
  this: Graph[T] =>

  def toGraphvizString(varMap: Map[T, _] = Map() withDefault (_.toString),
                       params: GraphvizParams = GraphvizParams()): String = {

    val sb = new StringBuilder

    //Header
    sb append "digraph {\n"
    sb append "graph [ rankdir=%s ];\n".format(params.rankDir)
    sb append "node [ shape=%s ];\n".format(params.shape)

    val fmap = Map() ++ (vertices.zipWithIndex map { case (f,i) => (f, i+1) })

    //Vertices
    for ((id, v) <- fmap.iterator.toList sortWith
      { case ((_,i),(_,j)) => i.toString < j.toString })
      sb append """%d [label="%s"]""".format(v, varMap(id).toString replace ("\"", "\\\"")) append "\n"

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



