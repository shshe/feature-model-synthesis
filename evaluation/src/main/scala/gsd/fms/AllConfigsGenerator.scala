package gsd.fms

import java.io.{PrintStream, FileFilter, File}
import de.tud.iai.modelcompare.splot.SxfmContextCreator

import collection.JavaConversions._
import colibri.lib.Relation

object AllConfigsGenerator {

  /**
   * @return time in nanoseconds
   */
  def nanoTime[T](f: => T): (T, Long) = {
    val t_ = System.nanoTime()
    val result = f
    val t__ = System.nanoTime()
    (result, t__ - t_)
  }

  def toContext(relation: Relation): Context = {
    import collection.JavaConversions._
    val attrMap = (relation.getAllAttributes zip (0 until relation.getSizeAttributes)).toMap
    val objMap = (relation.getAllObjects zip (0 until relation.getSizeObjects)).toMap

    val matrix = ( objMap map { case (obj, _) =>
      val posAttributes = relation.getAttributeSet(obj)

      val row = new Array[Boolean](relation.getSizeAttributes)
      for ((attr, pos) <- attrMap)
        row(pos) = posAttributes.contains(attr)

      row
    }).toArray

    Context(objMap.keys.map(_.toString).toArray,
            attrMap.keys.map(_.toString).toArray, matrix)
  }


  private case class Config(input: File = new File(getClass.getResource("../../splot").toURI),
                            threshold: Long = Long.MaxValue,
                            outputDir: String = "./cxt/")

  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("AlLConfigsGenerator", "1.0") {
      def options = Seq(
        opt("i", "input", "Input file") {
          (s: String, c: Config) => c.copy(new File(s))
        },
        opt("threshold", "Only models with less than this number of configurations will be generated") {
          (s: String, c: Config) => c.copy(threshold = s.toInt)
        },
        opt("o", "outputdir", "Directory to output CXT files") {
          (dir: String, c: Config) => c.copy(outputDir = dir)
        })
    }

    parser.parse(args, Config()) map { c =>

      val f = c.input
      val files: Array[File] =
        if (f.isFile)  Array(f)
        else f.listFiles(new FileFilter() {
        def accept(f: File) = f.getName endsWith (".xml")
      })

      for (file <- files) {
        println("Processing " + file.getName + "...")
        val contextCreator = new SxfmContextCreator(SxfmContextCreator.OutputType.NAME_ONLY)

        //
        // Load file
        //
        println("Load " + file.toString + "...")

        if (contextCreator.loadFile(file))  {
          val (relation, contextTime) = nanoTime(contextCreator.createContext())
          if (relation != null) {
            val cxt = toContext(relation)
            val outDir = new File(c.outputDir)
            if (!outDir.exists()) outDir.mkdir()
            val outFile = new File(outDir,
              contextCreator.getFeatureModelName.replaceAll("[\\/:*?\"<>|]", "_")  + ".cxt")
            val out = new PrintStream(outFile)

            out.println(cxt)
            out.close()
          }
          else println("Ignoring... (probably had too many configurations)")
        }
        else println("File load error!")
      }

      } getOrElse {}
  }

}
