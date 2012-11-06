package gsd.fms

import gsd.fms.dnf._
import dk.itu.fms.DNFAdapter

/**
 * Converts a feature model to it's set of configurations,
 * then outputs a CXT file
 */
object FeatureModelToCXT {

  private case class Config(
    inputFile: String = "",
    outputFile: String = ""
  )
  
  class FeatureModelConverter(fm: FeatureModel) {

    lazy val idMap = fm.idMap

    lazy val parentMap: Map[Int, Int] = {
      val tuples = new collection.mutable.ListBuffer[(Int, Int)]
      def _dfs(parent: Int)(curr: Node) {
        curr match {
          case n: NamedNode =>
            tuples += idMap(n.id) -> parent
            curr.children foreach _dfs(idMap(n.id))
          case _ =>
            curr.children foreach _dfs(parent)
        }
      }
      fm.root.children foreach _dfs(idMap(fm.root.id))
      tuples.toMap
    }

    def pathToRoot(f: Int): List[Int] = parentMap.get(f) match {
      case None => List(f)
      case Some(p) => f :: pathToRoot(p)
    }

    /**
     * This alone would be enough to identify the OR-group, however
     * this is actually an XOR-group.
     * @param parent
     * @param children
     * @return
     */
    def mkXorConfigs(parent: Int, children: List[Int]): Set[Set[Int]] =
      (for (child <- children) yield
         pathToRoot(child).toSet + parent
      ).toSet

    def mkHierarchyConfig(f: Int): Set[Int] =
      pathToRoot(f).toSet

    // FIXME mandatory features should not be optional
    lazy val hierarchyConfigs: Set[Set[Int]] =
      (fm.vars map mkHierarchyConfig).negateUnboundedVars(fm.maxVar).toSet

    // FIXME this overlaps with hierarchy configs - not needed
    lazy val groupConfigs =
      (fm.groups flatMap {
        case (parent, GroupNode(_,_,children)) =>
          mkXorConfigs(idMap(parent.id), children map (c => idMap(c.id)))
      }).negateUnboundedVars(fm.maxVar)


    /**
     * Assumes negated variables are added
     * @param configs
     * @return
     */
    def violatingMandatoryConfigs(configs: Set[Set[Int]]): Set[Set[Int]] =  {
      // Removes configs where a mandatory feature is not present when
      // its parent is present
      val presentToNotPresent: Map[Int, Set[Int]] = (fm.dfsWithParent {
        case (Some(p: NamedNode), MandNode(id,_,_)) =>
          idMap(p.id) -> Set(-idMap(id))
        case (Some(p: NamedNode), GroupNode(1,_,cs)) =>  
          idMap(p.id) -> (cs map (c => -idMap(c.id))).toSet
      }).toMap
      
      configs filter { config =>
        // true if the config contains a child but not the parent
        presentToNotPresent exists {
          case (present,notPresent) =>
            config.contains(present) && notPresent.subsetOf(config)
        }
      }
    }

    lazy val configs: Set[Set[Int]] =
      hierarchyConfigs ++ groupConfigs

  }

  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("FeatureModelToCXT", "1.0") {
      def options = Seq(
        arg("<file>", "Input feature model in SXFM format") {
          (file: String, c: Config) => c.copy(inputFile = file)
        },
        opt("o", "output", "Output CXT file") {
          (file: String, c: Config) => c.copy(outputFile = file)
        })
    }
    parser.parse(args, Config()) map { c =>
      // First, convert the feature model to its CNF representation
      val fm = SXFMParser.parseFile(c.inputFile)
      val converter = new FeatureModelConverter(fm)
      val dnf: Set[Set[Int]] = converter.configs
      dnf foreach (c => println(c.toList.sortBy(math.abs(_)).mkString(",")))

      val toRemove = converter.violatingMandatoryConfigs(converter.configs)
      println("Violating configs to remove: " + toRemove)

      println("Implication Graph")
      val g = new DNFImplBuilder(dnf -- toRemove, fm.ids.size).mkImplicationGraph()
      println(g)
      
      println("Feature Groups")
      val groups = DNFAdapter.orGroups(converter.groupConfigs)
      println(groups)

      // Generate using the SAT solver -- does not scale
      // val cnf = CNFSemantics.mkCNF(fm)
      // val sat = new SATBuilder(cnf, fm.ids.size)


    } getOrElse {
      // arguments are bad, usage message will have been displayed
    }
  }

}
