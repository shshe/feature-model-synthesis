package gsd.fms

case class FeatureModel(root: RootNode, constraints: List[Constraint]) {
  def print {
    println("Feature Tree")
    println("------------")
    root.printTree()
    println()
    println("Constraints")
    println("-----------")
    constraints foreach println
  }
}
case class Constraint(id: String,  expr: Expr)
