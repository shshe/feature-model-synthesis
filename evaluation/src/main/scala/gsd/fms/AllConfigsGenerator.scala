package gsd.fms

import java.io.{PrintStream, FileFilter, File}


object AllConfigsGenerator {

  private case class Config(inputDir: String = new File(getClass.getResource("../../splot").toURI).getCanonicalPath,
                            threshold: Long = Long.MaxValue,
                            outputDir: String = "./cxt/")
  
  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("AlLConfigsGenerator", "1.0") {
      def options = Seq(
        arg("threshold", "Only models with less than this number of configurations will be generated") {
          (s: String, c: Config) => c.copy(threshold = s.toInt)
        },
        opt("o", "outputdir", "Directory to output CXT files") {
          (dir: String, c: Config) => c.copy(outputDir = dir)
        })
    }

    parser.parse(args, Config()) map { c =>

      val dir = new File(c.inputDir)
      val files = dir.listFiles(new FileFilter() {
        def accept(f: File) = f.getName endsWith (".xml")
      })
      
      for (file <- files) {
        println("Processing " + file.getName + "...")
        val fm = SXFMParser.parseFile(file.getCanonicalPath)

        val s = new BDDSemantics
        val bdd = s.mkBDD(fm)
        if (bdd.satCount() < c.threshold) {
          val iter = s.allConfigurations(fm, bdd)
          val dnf = iter.toSet
          val cxt = Context.fromDNF(dnf, fm.varMap)

          val outDir = new File(c.outputDir)
          if (!outDir.exists()) outDir.mkdir()
          val outFile = new File(outDir, file.getName + ".cxt")
          val out = new PrintStream(outFile)

          out.println(cxt)
          out.close()
        }
        else {
          println("  Ignoring " + file.getName + "...")
        }
      }
    } getOrElse {}
  }

}
