package gsd.fms

import annotation.tailrec

case class FeatureModel(root: RootNode, constraints: List[Constraint]) {
  
  lazy val ids: List[String] = {
    def _ids(n: Node): List[String] = n match {
      case OptNode(id,_,cs) => id :: (cs flatMap _ids)
      case MandNode(id,_,cs) => id :: (cs flatMap _ids)
      case RootNode(id,_,cs) => id :: (cs flatMap _ids)
      case GroupNode(_,_,cs) => cs flatMap _ids
    }
    _ids(root).distinct
  }
  
  lazy val idMap: Map[String, Int] =
    (ids zip (1 to ids.size)).toMap
  
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
case class Constraint(id: String,  expr: Expr) {
  override def toString =
    id + ": " + expr.toString
}
