//package gsd.fms
//
//import dnf.DNFImplBuilder
//import io.Source
//
//object CXTParser {
//
//  def parse(in: Iterator[String]): Context = {
//
//    val lines = in.withFilter(!_.trim.isEmpty)
//
//    // Initial 'B'
//    val b = lines.next()
//
//    // number of objects
//    val numObjs = lines.next().toInt
//
//    // number of attributes
//    val numAttrs = lines.next().toInt
//
//    // parse objects
//    val objects = ((1 to numObjs) map (_ => lines.next())).toArray
//
//    // parse attributes
//    val attributes = ((1 to numAttrs) map (_ => lines.next())).toArray
//
//    // parse context
//    def parseEntry(i: Char): Boolean = i == 'X'
//
//    val context = new Array[Array[Boolean]](numObjs)
//    for (i <- 0 until numObjs) {
//      val line = lines.next()
//      context(i) = new Array[Boolean](numAttrs)
//      for (j <- 0 until numAttrs) {
//        context(i)(j) = parseEntry(line(j))
//      }
//    }
//    Context(objects, attributes, context)
//  }
//
//  def parse(file: String): Context =  {
//    val source = Source.fromFile(file)
//    val result = parse(source.getLines())
//    source.close()
//    result
//  }
//
//  def main(args: Array[String]) {
//    val context = CXTParser.parse(args(0))
//    println(context.toString)
//    println()
//    val dnf = context.toDNF
//    dnf foreach (x => println(x.toList.sortBy(math.abs(_)).mkString(",")))
//
//    // First, identify hierarchy
//    val impls = new DNFImplBuilder(dnf, context.attributes.size)
//    println(impls.implications)
//
//    // Next, compute or-groups
//  }
//
//}
