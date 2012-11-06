package gsd.fms

import gsd.fms.sat._

import sat.SATBuilder
import java.io.{PrintStream, FileFilter, File}

object SPLOTStatistics {
  
  def mkFM(file: String): FeatureModel =
    SXFMParser.parseFile(file)

  def mkSATBuilder(fm: FeatureModel): SATBuilder = {
    val cnf = CNFSemantics.mkCNF(fm)
    new SATBuilder(cnf, cnf.vars.size)
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

  def main(args: Array[String]) {
    val out = if (args.size > 0) new PrintStream(args(0))
              else System.out
    
    val dir = new File(getClass.getResource("../../splot").toURI)
    val files = dir.listFiles(new FileFilter() {
      def accept(f: File) = f.getName endsWith (".xml")
    })
    
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

    if (out != System.out)
      out.close()
  }
  
}
