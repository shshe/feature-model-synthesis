package gsd.fms

import io.Source

object CXTParser {

  case class Context(objects: Array[String],
                     attributes: Array[String],
                     matrix: Array[Array[Boolean]]) {
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
  }

  def parse(in: Iterator[String]): Context = {

    val lines = in.withFilter(!_.trim.isEmpty)

    // Initial 'B'
    val b = lines.next()

    // number of objects
    val numObjs = lines.next().toInt

    // number of attributes
    val numAttrs = lines.next().toInt

    // parse objects
    val objects = ((1 to numObjs) map (_ => lines.next())).toArray

    // parse attributes
    val attributes = ((1 to numAttrs) map (_ => lines.next())).toArray

    // parse context
    def parseEntry(i: Char): Boolean = i == 'X'
    
    val context = new Array[Array[Boolean]](numObjs)
    for (i <- 0 until numObjs) {
      val line = lines.next()
      context(i) = new Array[Boolean](numAttrs)
      for (j <- 0 until numAttrs) {
        context(i)(j) = parseEntry(line(j))
      }
    }
    Context(objects, attributes, context)
  }
  
  def parse(file: String): Context = 
    parse(Source.fromFile(file).getLines())
  
  def main(args: Array[String]) {
    val context = CXTParser.parse(args(0))
    println(context.toString)
  }

}
