package gsd.fms

abstract class Node(val children: List[Node]) {
  def printTree() {
    def p(level: Int)(n: Node) {
      (0 until level).foreach(_ => print("  "))
      n match {
        case RootNode(id, name, _) =>
          print("r: %s (%s)".format(id, name))
        case OptNode(id, name, _) =>
          print("o: %s (%s)".format(id, name))
        case MandNode(id, name, _) =>
          print("m: %s (%s)".format(id, name))
        case GroupNode(min, None, _) =>
          print("g: [%d,%s]".format(min, "*"))
        case GroupNode(min, Some(max), _) =>
          print("g: [%d,%d]".format(min, max))
      }
      println()
      n.children foreach p(level + 1)
    }
    p(0)(this)
  }

}

abstract class NamedNode(val id: String,
                         val name: String,
                         children: List[Node]) extends Node(children)

case class RootNode(override val id: String,
                    override val name: String,
                    private val cs: List[Node])
  extends NamedNode(id, name, cs)

case class OptNode(override val id: String,
                   override val name: String,
                   private val cs: List[Node])
  extends NamedNode(id, name, cs)

case class MandNode(override val id: String,
                    override val name: String,
                    private val cs: List[Node])
  extends NamedNode(id, name, cs)

case class GroupNode(min: Int,
                     max: Option[Int],
                     private val cs: List[OptNode])
  extends Node(cs)

