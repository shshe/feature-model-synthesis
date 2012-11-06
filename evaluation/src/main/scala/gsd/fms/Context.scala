package gsd.fms

case class Context(objects: Array[String],
                   attributes: Array[String],
                   matrix: Array[Array[Boolean]]) {

  val maxVar = attributes.size

  import gsd.fms.dnf._

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
    for (i <- 0 until matrix.length) yield {
      val row = for (j <- 0 until matrix(i).length) yield
        if (matrix(i)(j)) j+1
        else -(j+1)
      row.toSet
    }
}

