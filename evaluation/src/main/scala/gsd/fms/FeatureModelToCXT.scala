//package gsd.fms
////
////import gsd.fms.dnf._
//import java.io.{File, PrintStream}
//import de.tud.iai.modelcompare.splot.SxfmContextCreator
//
////import org.sat4j.tools.ModelIterator
////
/////**
//// * Converts a feature model to it's set of configurations,
//// * then outputs a CXT file
//// */
//object FeatureModelToCXT {
////
////  class FeatureModelConverter(fm: FeatureModel) {
////    lazy val idMap = fm.idMap
////
////    lazy val parentMap: Map[Int, Int] = {
////      val tuples = new collection.mutable.ListBuffer[(Int, Int)]
////      def _dfs(parent: Int)(curr: Node) {
////        curr match {
////          case n: NamedNode =>
////            tuples += idMap(n.id) -> parent
////            curr.children foreach _dfs(idMap(n.id))
////          case _ =>
////            curr.children foreach _dfs(parent)
////        }
////      }
////      fm.root.children foreach _dfs(idMap(fm.root.id))
////      tuples.toMap
////    }
////
////    lazy val mandatorySiblings: Map[Int, Set[Int]] = {
////      val tuples = new collection.mutable.HashMap[Int, Set[Int]]()
////      fm.dfs { case n: Node =>
////          val mands = n.children.collect {
////            case MandNode(id,_,_) => idMap(id)
////          }
////          val opts = n.children.collect {
////            case OptNode(id,_,_) => idMap(id)
////          }
////          for (o <- opts ++ mands)
////            tuples += o -> mands.toSet
////      }
////      tuples.toMap withDefaultValue (Set())
////    }
////
////    /**
////     * FIXME not used
////     * A map from a node to its mandatory subfeatures
////     *
////     * FIXME should this also include xor-groups?
////     */
////    lazy val mandatorySubfeatureMap: Map[Int, Set[Int]] = {
////      import collection.mutable._
////      val tuples = new HashMap[Int, Set[Int]]() with MultiMap[Int, Int]
////      fm.dfsWithParent {
////        case (Some(n: NamedNode), m:MandNode) =>
////          tuples.addBinding(idMap(n.id), idMap(m.id))
////      }
////      (tuples mapValues (_.toSet)).toMap withDefaultValue
////        (collection.immutable.Set[Int]())
////    }
////
////    def pathToRoot(f: Int): List[Int] = parentMap.get(f) match {
////      case None => List(f)
////      case Some(p) => f :: pathToRoot(p)
////    }
////
////    /**
////     * This alone would be enough to identify the OR-group, however
////     * this is actually an XOR-group.
////     * @param parent
////     * @param children
////     * @return
////     */
////    def mkXorConfigs(parent: Int, children: List[Int]): DNF =
////      (for (child <- children) yield
////         pathToRoot(child).toSet + parent
////      ).toSet
////
////    def mkHierarchyConfig(f: Int): Set[Int] =
////      pathToRoot(f).toSet ++ mandatorySiblings(f)
////
////    lazy val hierarchyConfigs: DNF =
////      (fm.vars map mkHierarchyConfig).negateUnboundedVars(fm.maxVar)
////
////    // FIXME this overlaps with hierarchy configs - not needed
////    lazy val groupConfigs: DNF =
////      ((fm.groups flatMap {
////        case (parent, GroupNode(_,_,children)) =>
////          mkXorConfigs(idMap(parent.id), children map (c => idMap(c.id)))
////      }).toSet: DNF).negateUnboundedVars(fm.maxVar)
////
////
////    /**
////     * Assumes negated variables are added
////     * @param configs
////     * @return
////     */
////    def violatingConfigs(configs: Set[Set[Int]]): Set[Set[Int]] =  {
////      // Removes configs where a mandatory feature is not present when
////      // its parent is present
////      val presentToNotPresent: Map[Int, Set[Int]] = (fm.dfsWithParent {
////        case (Some(p: NamedNode), MandNode(id,_,_)) =>
////          idMap(p.id) -> Set(-idMap(id))
////        // case (Some(p: NamedNode), GroupNode(1,_,cs)) =>
////        //  idMap(p.id) -> (cs map (c => -idMap(c.id))).toSet
////      }).toMap
////
////      configs filter { config =>
////        // true if the config contains a child but not the parent
////        presentToNotPresent exists {
////          case (present,notPresent) =>
////            config.contains(present) && notPresent.subsetOf(config)
////        }
////      }
////    }
////
////    lazy val configs: Set[Set[Int]] =
////      hierarchyConfigs ++ groupConfigs
////
////  }
////
////
////  class AllConfigurationsConverter(fm: FeatureModel) {
////
////    import gsd.fms.sat._
////
////    lazy val configs: Set[Set[Int]] = {
////      val cnf = CNFSemantics.mkCNF(fm)
////      val sat = new SATBuilder(cnf, cnf.vars.size)
////      val mi = new ModelIterator(sat.solver)
////
////      val configs = new collection.mutable.ListBuffer[Set[Int]]
////      while (mi.isSatisfiable)
////        configs += mi.model().toSet
////      configs.toSet
////    }
////
////  }
////
//  private case class Config(inputFile: File = "",
//                            out: PrintStream = System.out)
//
//
//  def main(args: Array[String]) {
//    val parser = new scopt.immutable.OptionParser[Config]("FeatureModelToCXT", "1.0") {
//      def options = Seq(
//        arg("<file>", "Input feature model in SXFM format") {
//          (file: String, c: Config) => c.copy(inputFile = file)
//        },
//        opt("o", "output", "Output CXT file") {
//          (file: String, c: Config) => c.copy(out = new PrintStream(file))
//        })
//    }
//    parser.parse(args, Config()) map { c =>
//      // First, convert the feature model to its CNF representation
//
//      val contextCreator = new SxfmContextCreator(SxfmContextCreator.OutputType.NAME_ONLY)
//
//      //
//      // Load file
//      //
//      println("Load " + c.file.toString + "...")
//      if (!contextCreator.loadFile(c.file)) sys.error("File load failed!")
//
//      //
//      // Generate all configurations as a context
//      //
//      val (context, contextTime) = nanoTime(contextCreator.createContext())
//
//
//
//      val fm = SXFMParser.parseFile(c.inputFile)
//      val converter = new FeatureModelConverter(fm)
//      val dnf = converter.configs
//      val remove = converter.violatingConfigs(converter.configs)
//
//      val cxt = Context.fromDNF(dnf -- remove, fm.varMap)
//      c.out.println(cxt)
//
//      println(converter.violatingConfigs(dnf))
//
//      println()
//
//      val allConfigs = new AllConfigurationsConverter(fm)
//      val cxt2 = Context.fromDNF(allConfigs.configs, fm.varMap)
//      c.out.println(cxt2)
//
//    } getOrElse {}
//  }
//
//}
