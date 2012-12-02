package gsd.fms

import gsd.fms.sat._

import sat.SATBuilder
import java.io.{PrintStream, FileFilter, File}
import net.sf.javabdd.BDD

object SPLOTStatistics {
  
  def mkFM(file: String): FeatureModel =
    SXFMParser.parseFile(file)

  def mkSATBuilder(fm: FeatureModel): SATBuilder = {
    val cnf = CNFSemantics.mkCNF(fm)
    new SATBuilder(cnf, cnf.vars.size)
  }
  
  def mkBDD(fm: FeatureModel): BDD = {
    val s = new BDDSemantics
    s.mkBDD(fm)
  }

  implicit def toRichFeatureModel(fm: FeatureModel) = new {

    def orGroups = fm.dfs {
      case x@GroupNode(1,None, _) => x
    }
    def xorGroups = fm.dfs {
      case x@GroupNode(1,Some(1), _) => x
    }
    def otherGroups = (fm.dfs {
      case x@GroupNode(1,None, _) => None
      case x@GroupNode(1,Some(1), _) => None
      case x@GroupNode(_,_, _) => Some(x)
    }).flatten
  }
  
  private case class Config(stat: Stats = GroupStats,
                            out: PrintStream = System.out)

  sealed trait Stats {
    def process(out: PrintStream, files: Seq[File])
  }
  case object GroupStats extends Stats {
    def process(out: PrintStream, files: Seq[File]) {
      out.println("name,features,groups,type")
      for (file <- files) {
        val fm = mkFM(file.getCanonicalPath)
        out.println("%s,%d,%d,%s".format(
          file.getName,
          fm.features.size,
          fm.orGroups.size,
          "OR-Groups"))
        out.println("%s,%d,%d,%s".format(
          file.getName,
          fm.features.size,
          fm.xorGroups.size,
          "XOR-Groups"))
      }
    }
  }

  case object ConfigStats extends Stats {
    def process(out: PrintStream, files: Seq[File]) {
      out.println("name,features,groups,implications,logconfigs,configs")
      for (file <- files) {
        println("Working on " + file.getName + "...")
        val fm = mkFM(file.getCanonicalPath)

        val bdd = mkBDD(fm)
        val domain = (fm.vars map (bdd.getFactory.ithVar))
                       .foldLeft(bdd.getFactory.one())(_.andWith(_))
        out.println("%s,%d,%d,%d,%f,%f".format(
          file.getName,
          fm.features.size,
          fm.groups.size,
          fm.numImplications,
          bdd.logSatCount(domain),
          bdd.satCount(domain)))
        bdd.free()
      }
    }

  }
  
  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("SPLOTStatistics", "1.0") {
      def options = Seq(
        arg("<stat>", "One of: groups, configs") {
          (s: String, c: Config) => s match {
            case "group"   => c.copy(stat = GroupStats)
            case "configs" => c.copy(stat = ConfigStats)
          }
        },
        opt("o", "output", "Output file") {
          (file: String, c: Config) => c.copy(out = new PrintStream(file))
        })
    }

    parser.parse(args, Config()) map { c =>
      val dir = new File(getClass.getResource("../../splot").toURI)
      val files = dir.listFiles(new FileFilter() {
        def accept(f: File) = f.getName endsWith (".xml")
      })

      c.stat.process(c.out, files)

      if (c.out != System.out)
        c.out.close()
    } getOrElse {}

  }
  
}
