package gsd.fms

import colibri.lib.{Concept, Relation}

import dk.itu.fms.formula.dnf.{DNFClause, DefaultDNFSolver, DNF => ITUDNF}
import java.io.{FileFilter, PrintStream, File}
import gsd.graph.DirectedGraph
import dk.itu.fms.prime.Prime
import de.tud.iai.modelcompare.splot.{ContextCreator, SxfmContextCreator, SxfmRelationFinder, CxtContextCreator}

import collection.JavaConversions._
import de.tud.iai.modelcompare.fca.{AttributeConceptGraph, ManyObjectsFCAUtil}
import java.util

object DNFEvaluation {


  /**
   * @return time in nanoseconds
   */
  def nanoTime[T](f: => T): (T, Long) = {
    val t_ = System.nanoTime()
    val result = f
    val t__ = System.nanoTime()
    (result, t__ - t_)
  }

  def inMs(time: Long): Long =
    time / 1000000

  def toITUDNF(context: Relation): (ITUDNF, Map[Comparable[_], Int]) = {
    import collection.JavaConversions._

    val attrMap =
      (context.getAllAttributes zip (1 to context.getSizeAttributes)).toMap

    val terms = for (obj <- context.getAllObjects) yield {
      val posAttributes = context.getAttributeSet(obj)
      val negAttributes = (attrMap.keySet -- posAttributes)
      val posLiterals = (posAttributes map attrMap).toSet
      val negLiterals = (negAttributes map attrMap map (-_)).toSet

      new DNFClause((posLiterals ++ negLiterals).toArray)
    }

    (new ITUDNF(terms), attrMap)
  }

  sealed trait Algorithm {
    def compute(c: Config, file: File, context: Relation)
  }

  case object UweAlgorithm extends Algorithm {
    override def compute(c: Config, file: File, context: Relation) {
      val contextCreator = new CxtContextCreator()
      val (lattice, latticeTime) = nanoTime {
        val l = contextCreator.createLattice(context)
        new ManyObjectsFCAUtil(l); // creates also new attribute cache
        l
      }
      val fcaUtil = new ManyObjectsFCAUtil(lattice) // creates also new attribute cache

      val allAttributes = fcaUtil.castComparableSet(fcaUtil.getBottomConcept.getAttributes)
      val attributes = new util.HashSet[String](allAttributes)

      // dead features
      System.out.println("Dead features:")
      if (lattice.bottom().getObjects.isEmpty) {
        for (obj <- fcaUtil.getAddedAttributes(lattice.bottom())) {
          val deadFeature = obj.asInstanceOf[String]
          println(" * " + contextCreator.getFeatureName(deadFeature))
          attributes.remove(deadFeature)
        }
      }

      // mandatory feature set
      System.out.println("Mandatory features:")
      val newAttributeMap = new util.HashMap[Concept, String]

      for (concept <- fcaUtil.getAllAttributeConcepts(attributes)) {
        val equivAttrs = fcaUtil.castComparableSet(fcaUtil.getAddedAttributes(concept))
        if (equivAttrs.size() > 1) {
          val sorted = new util.ArrayList[String](equivAttrs)
          util.Collections.sort(sorted)
          System.out.println(sorted.mkString("* {", ",", "}"))
          attributes.removeAll(sorted.subList(1, sorted.size()))
          newAttributeMap.put(concept, sorted.get(0))
        }
        else if (equivAttrs.size() == 1) {
          newAttributeMap.put(concept, equivAttrs.iterator().next())
        }
      }

      println("Number of living non-mandatory features: " + attributes.size())


      val (implg, implicationsTime) = nanoTime(new AttributeConceptGraph(fcaUtil, attributes, true))
      System.out.println("Time to compute attribute concepts graph (= implication graph): " + inMs(implicationsTime) + " ms")
      val implicationsCount = implg.getEdges.size()

      c.out.println("%s,%d,%d,%s,%d,Implications".format(
        file.getName, context.getSizeObjects, context.getSizeAttributes, inMs(implicationsTime), implicationsCount))

      val relFinder = new SxfmRelationFinder(fcaUtil, implg)
      val (_, groupTime) = nanoTime {
        relFinder.findOrAndAlternativeConcepts(false)
      }

      val groupCount = relFinder.getIdentifiedRelationGroups().size()

      def printGroups() {
        for (relationGroup <- relFinder.getIdentifiedRelationGroups) {
          val str = new StringBuilder

          val precon = newAttributeMap.get(relationGroup.getPreconditionObject)
          str.append(" * ")
          str.append(if(precon == null) "<ROOT>" else contextCreator.getFeatureName(precon))
          str.append(" <-> ")
          str.append(relationGroup.getRelationType)
          str.append('(')

          var firstLoop = true
          for (concept <- relationGroup.getRelationedObjects)
          {
            if (firstLoop)
              firstLoop = false
            else
              str.append(",")

            str.append(contextCreator.getFeatureName(newAttributeMap.get(concept)))
          }
          str.append(')')

          println(str.toString())
        }
      }

      printGroups()

      System.out.println("Time to compute or and xor groups: " + inMs(groupTime) + " ms")

      c.out.println("%s,%d,%d,%s,%d,Groups".format(
        file.getName, context.getSizeObjects, context.getSizeAttributes, inMs(groupTime), groupCount))
    }
  }

  case object GSDAlgorithm extends Algorithm {
    override def compute(c: Config, file: File, context: Relation) {
      val (ituDNF, attrMap) = toITUDNF(context)
      val varMap = (attrMap map (_.swap)).toMap

      val (implications, implicationsTime) = nanoTime {
        val results = new collection.mutable.ListBuffer[(Int, Int)]
        for (i <- ituDNF.getVariables) {
          for (j <- ituDNF.getVariables if i != j) {
            if (ituDNF.implication(i, j)) results += i -> j
          }
        }
        results
      }

      println("Time to compute implications: " + inMs(implicationsTime) + "ms")

      val implg = new DirectedGraph[Int](
        ituDNF.getVariables.toSet,
        implications  groupBy (_._1) mapValues
          (_ map (_._2) toSet) withDefault (_ => Set.empty[Int]))

      if (c.outputImplicationGraph) {
        val implFile = file.getName + ".dot"
        println("Outputting implication graph to: " + implFile)
        val implOut = new PrintStream(implFile)
        println("VarMap: " + varMap(1))
        implOut.println(
          implg.collapseCliques.transitiveReduction.expandCliques.toGraphvizString(varMap))
        implOut.close()
      }

      println()
      println("Collapsing cliques...")
      val cliqueg: DirectedGraph[Set[Int]] = implg.collapseCliques

      println()
      println("Computing groups...")
      var totalGroupTime = 0L
      val groups = new collection.mutable.ListBuffer[(Int, java.util.Set[java.lang.Integer])]

      for (clique <- cliqueg.vertices) {
        val i = clique.head
        //
        // Retain only descendants in the formula
        //
        val retainVars = cliqueg.revEdges(clique).map(_.head) + i
        val eliminateVars = implg.vertices -- retainVars

        // Initialize DNF solver
//        val (solver, solverTime) = nanoTime(new DefaultDNFSolver(ituDNF, i))

        // Returns Java sets
        val (primes, groupTime) = nanoTime {
          val (solver, solverTime) = nanoTime {
            new DefaultDNFSolver(
            ituDNF,
            i,
            eliminateVars map (new java.lang.Integer(_)))
          }
          val p = new Prime(solver)

          if (c.additionalTimes) println("Working on " + varMap(i))
          if (c.additionalTimes) println("Solver initialization:  " + inMs(solverTime) + "ms")

          p.setCollectTimes(c.additionalTimes)
          val result = p.positivePrimes

          if (c.additionalTimes) println("BIP algorithm:          " + p.getTimes.map(_.toLong).sum + "ms")
          result
        }
        totalGroupTime += groupTime

        groups ++= primes filter (_.size > 1) map (i -> _)
      }

      println("Time to compute groups: " + inMs(totalGroupTime) + "ms")

      def printGroups() {
        for ((parent, group) <- groups) {
          println("Group: " + varMap(parent) + " -> " + group.map(varMap(_)).mkString(","))
        }
      }
      printGroups()

      c.out.println("%s,%d,%d,%s,%d,Implications".format(
        file.getName, ituDNF.size, ituDNF.getNumberOfVariables, inMs(implicationsTime), implications.size))
      c.out.println("%s,%d,%d,%s,%d,Groups".format(
        file.getName, ituDNF.size, ituDNF.getNumberOfVariables, inMs(totalGroupTime), groups.size))
    }
  }


  private case class Config(input: File = new File(getClass.getResource("../../cxt").toURI),
                            out: PrintStream = System.out,
                            additionalTimes: Boolean = false,
                            outputImplicationGraph: Boolean = false,
                            algorithm: Algorithm = GSDAlgorithm) {
    override def toString: String = {
      val sb = new StringBuilder
      sb append "Input:            %s\n".format(input)
      sb append "Output:           %s\n".format(out.toString)
      sb append "Additional Times: %s\n".format(additionalTimes)
      sb.toString()
    }
  }


  /**
   * @param args args(0) is the file to write the output too
   */
  def main(args: Array[String]) {
    val parser = new scopt.immutable.OptionParser[Config]("DNFEvaluation", "1.0") {
      def options = Seq(
        opt("f", "input", "Input file or directory containing .cxt or .xml files") {
          (file: String, c: Config) => c.copy(input = new File(file))
        },
        opt("out", "Output CSV file") {
          (file: String, c: Config) => c.copy(out = new PrintStream(file))
        },
        flag("g", "implication-graph", "output implication graph as Graphviz .dot file") {
          (c: Config) => c.copy(outputImplicationGraph = true)
        },
        flag("t", "additional-times", "Measure additional times (e.g. BIP solver for GSD algorithm)") {
          (c: Config) => c.copy(additionalTimes = true)
        },
        arg("<uwe|gsd>", "name of algorithm to run") {
          (v: String, c: Config) => v match {
            case "uwe" => c.copy(algorithm = UweAlgorithm)
            case "gsd" => c.copy(algorithm = GSDAlgorithm)
          }
        }
      )
    }
    parser.parse(args, Config()) map { c =>
      println(c)

      val files = if (c.input.isFile) Array(c.input)
        else c.input.listFiles(new FileFilter() {
          def accept(f: File) = (f.getName endsWith (".cxt")) || (f.getName endsWith (".xml"))
        })

      //
      // print header, iterate through each input file
      //
      c.out.println("name,objects,attributes,time,num,type")
      for (file <- files.sortBy(_.getName)) {
        println("Processing " + file.getName + "...")

        val creator: ContextCreator =
          if (file.getName endsWith (".xml"))
            new SxfmContextCreator(SxfmContextCreator.OutputType.NAME_ONLY)
          else new CxtContextCreator

        creator.loadFile(file)
        val context = creator.createContext()
        c.algorithm.compute(c, file, context)
      }
      c.out.flush()
    } getOrElse {}
  }

}
