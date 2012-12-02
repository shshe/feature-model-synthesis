package gsd.fms

case class FeatureModel(root: RootNode, constraints: List[Constraint]) {

  lazy val ids: List[String] = {
    def _ids(n: Node): List[String] = n match {
      case OptNode(id, _, cs) => id :: (cs flatMap _ids)
      case MandNode(id, _, cs) => id :: (cs flatMap _ids)
      case RootNode(id, _, cs) => id :: (cs flatMap _ids)
      case GroupNode(_, _, cs) => cs flatMap _ids
    }
    _ids(root).distinct
  }
  
  lazy val groups: Map[NamedNode, GroupNode] =
    (dfsWithParent {
      case (Some(parent: NamedNode), g: GroupNode) =>
        parent -> g
    }).toMap

  lazy val features: List[Node] = dfs {
    case x => x
  }

  lazy val numImplications: Int = {
    var num = 0
    def _dfs(child: Node, depth: Int) {
      num += depth
      for (c <- child.children)  _dfs(c, depth + 1)
    }
    _dfs(root, 0)
    num
  }

  lazy val idMap: Map[String, Int] =
    (ids zip (1 to ids.size)).toMap
  
  lazy val varMap: Map[Int, String] =
    (idMap map (_.swap)).toMap

  lazy val vars: Set[Int] =
    idMap.values.toSet

  lazy val maxVar: Int =
    ids.size
  
  def print() {
    println("Feature Tree")
    println("------------")
    root.printTree()
    println()
    println("Constraints")
    println("-----------")
    constraints foreach println
  }
  
  /**
   * @param f is a partial function from a parent (None for the root) and
   *        a child to a result.
   */
  def dfsWithParent[A](f: PartialFunction[(Option[Node], Node), A]): List[A] = {
    val result = new collection.mutable.ListBuffer[A]
    def dfsNode(parent: Option[Node], child: Node) {
      if (f.isDefinedAt((parent,child))) result += f((parent,child))
      for (c <- child.children)  dfsNode(Some(child), c)
    }
    dfsNode(None, root)
    result.toList
  }

  def dfs[A](f: PartialFunction[Node, A]): List[A] = dfsWithParent(
    new PartialFunction[(Option[Node], Node), A] {
        def apply(x: (Option[Node], Node)) = f(x._2)
        def isDefinedAt(x: (Option[Node], Node)) = f.isDefinedAt(x._2)
      })
}

case class Constraint(id: String, expr: Expr) {
  override def toString =
    id + ": " + expr.toString
}
