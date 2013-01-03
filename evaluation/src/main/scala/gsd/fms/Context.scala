package gsd.fms

import gsd.fms.dnf._

case class Context(objects: Array[String],
                   attributes: Array[String],
                   matrix: Array[Array[Boolean]]) {

  val maxVar = attributes.size

  override def toString: String = {
    val sb = new StringBuilder
    sb append "B\n"
    sb append objects.size append "\n"
    sb append attributes.size append "\n"
    objects foreach (sb.append(_).append("\n"))
    attributes foreach (sb.append(_).append("\n"))
    for (row <- matrix) {
      for (value <- row) {
        if (value) sb append 'X'
        else sb append '.'
      }
      sb append "\n"
    }
    sb.toString()
  }

  def toDNF: DNF =
    for (i <- (0 until matrix.length).toSet: Set[Int]) yield {
      val row = for (j <- 0 until matrix(i).length) yield
        if (matrix(i)(j)) j+1
        else -(j+1)
      row.toSet
    }
}

object Context {
  /**
   * Assumes negated variables have been added
   */
  implicit def fromDNF(dnf: DNF,
                       attributeMap: Map[Int, String] = Map() withDefault (_.toString)): Context = {
    val attributes = ((1 to dnf.maxVar) map (attributeMap.apply)).toArray

    val matrix = (dnf map {
      case config => 
        val row = new Array[Boolean](dnf.maxVar)
        config map {
          case x if x > 0 => row(x-1) = true
          case x if x < 0 => row(math.abs(x)-1) = false
          case 0 => sys.error("DNF representation should never contain 0")
        }
        row
    }).toArray

    Context(((1 to matrix.size) map (_.toString)).toArray, attributes, matrix)
  }

}
