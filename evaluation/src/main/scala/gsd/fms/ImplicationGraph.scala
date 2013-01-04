//package gsd.fms
//
//import dnf.DNFImplBuilder
//import java.io.PrintStream
//
//object ImplicationGraph {
//
//  case class Config(inputFile: String = "",
//                    out: PrintStream = System.out)
//
//  def main(args: Array[String]) {
//    val parser = new scopt.immutable.OptionParser[Config]("ImplicationGraph", "1.0") {
//      def options = Seq(
//        arg("<file>", "Input CXT file") {
//          (file: String, c: Config) => c.copy(inputFile = file)
//        },
//        opt("o", "output", "Output file") {
//          (file: String, c: Config) => c.copy(out = new PrintStream(file))
//        })
//
//    }
//
//    parser.parse(args, Config()) map { c =>
//      val context = CXTParser.parse(c.inputFile)
//      val dnf = context.toDNF
//      val b = new DNFImplBuilder(dnf, context.attributes.size)
//      b.implications foreach println
//    } getOrElse {}
//
//  }
//
//}
