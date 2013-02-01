package gsd.fms

import gsd.fms.sat._

import sat.SATBuilder
import java.io.{PrintStream, FileFilter, File}
import net.sf.javabdd.BDD
import de.tud.iai.modelcompare.splot.{SxfmContextCreator, CxtContextCreator}
import collection.mutable

object SPLOTStatistics {
  
  def mkFM(file: String): FeatureModel =
    SXFMParser.parseFile(file)

  def mkSATBuilder(fm: FeatureModel): SATBuilder = {
    val cnf = CNFSemantics.mkCNF(fm)
    new SATBuilder(cnf, cnf.vars.size)
  }
  
  def mkBDD(fm: FeatureModel, s: BDDSemantics): BDD = {
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
  
  private case class Config(stat: Stats = ConfigStats,
                            out: PrintStream = System.out)

  sealed trait Stats {
    def process(out: PrintStream, files: Seq[File])
  }

  case object GroupStats extends Stats {
    def process(out: PrintStream, files: Seq[File]) {
      for (file <- files) {
        println("Working on " + file.getName + "...")
        val fm = mkFM(file.getCanonicalPath)
        val specialGroups = fm.groups.values filter {g =>
          g.min > 1 || g.max.isDefined && g.max.get != 1 && g.max.get < g.members.size
        }
        if (specialGroups.size > 0) specialGroups foreach println
      }
    }
  }

  case object ConfigStats extends Stats {
    def process(out: PrintStream, files: Seq[File]) {
      out.println("filename,splotname,features,groups,grouped,implyingsum,logconfigs,configs")
      for (file <- files) {
        println("Working on " + file.getName + "...")

//        val creator = new SxfmContextCreator(SxfmContextCreator.OutputType.NAME_ONLY)
//        creator.loadFile(file)


        val fm = mkFM(file.getCanonicalPath)

        val s = new BDDSemantics
        val bdd = mkBDD(fm, s)

        // Compute implications
        val implying = new mutable.HashMap[Int, mutable.Set[Int]] with mutable.MultiMap[Int, Int]
        if (fm.vars.size < 100) {
        for (i <- fm.vars; j <- fm.vars if i != j) {
          val temp = bdd.id().andWith(s.factory.ithVar(i)).andWith(s.factory.nithVar(j))
          if (temp.isZero) implying.addBinding(j, i) // i implies j, we add the reverse to the implying map
          temp.free()
        }
        }
        else {
          println("Not computing implications since there are more than 100 features")
        }

        val implyingsum: Int = implying.values.map(_.size).sum

        val domain = (fm.vars map (bdd.getFactory.ithVar))
                       .foldLeft(bdd.getFactory.one())(_.andWith(_))
        out.println("%s,\"%s\",%d,%d,%d,%d,%f,%f".format(
          file.getName,
          fm.name,
          fm.features.size,
          fm.groups.size,
          fm.grouped.size,
          implyingsum,
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
            case "configs" => c.copy(stat = ConfigStats)
            case "groups" => c.copy(stat = GroupStats)
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
