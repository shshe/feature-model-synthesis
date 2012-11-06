package gsd.fms

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import sat.SATBuilder
import java.util.Arrays
import org.sat4j.tools.ModelIterator

class CNFSemanticsTest extends FunSuite with ShouldMatchers {

  implicit def toId(s: String): Id =
    Id(s)

  test("Optional features") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        OptNode("o1", "o1", Nil),
        OptNode("o2", "o2", Nil)
      )),
      Nil
    )
    val cnf = CNFSemantics.mkCNF(fm)

    cnf should have size 2
    cnf should contain(Set(-2, 1))
    cnf should contain(Set(-3, 1))
  }

  test("Mandatory feature") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        MandNode("o1", "o1", Nil)
      )),
      Nil
    )
    val cnf = CNFSemantics.mkCNF(fm)

    cnf should have size 2
    cnf should contain(Set(-2, 1))
    cnf should contain(Set(-1, 2))
  }

  test("OR-group") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        OptNode("o", "o", List(
          GroupNode(1, None, List(
            OptNode("g1", "g1", Nil),
            OptNode("g2", "g2", Nil)
          ))
        ))
      )),
      Nil
    )
    val cnf = CNFSemantics.mkCNF(fm)

    cnf should contain(Set(-2, 1))
    cnf should contain(Set(-3, 2))
    cnf should contain(Set(-4, 2))
    cnf should contain(Set(-2, 3, 4))
  }

  test("XOR-group") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        OptNode("o", "o", List(
          GroupNode(1, Some(1), List(
            OptNode("g1", "g1", Nil),
            OptNode("g2", "g2", Nil),
            OptNode("g3", "g3", Nil)
          ))
        ))
      )),
      Nil
    )
    val cnf = CNFSemantics.mkCNF(fm)

    cnf should contain(Set(-2, 3, 4, 5))
    cnf should contain(Set(-3, -4))
    cnf should contain(Set(-4, -5))
    cnf should contain(Set(-5, -3))
  }

  test("Cross-tree constraints") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        OptNode("o1", "o1", Nil),
        OptNode("o2", "o2", Nil),
        OptNode("o3", "o3", Nil)
      )),
      List(
        Constraint("c1", Id("o1") imp Id("o2")),
        Constraint("c2", Id("o2") imp Id("o3"))
      )
    )

    val cnf = CNFSemantics.mkCNF(fm)

    cnf should contain(Set(-2, 3))
    cnf should contain(Set(-3, 4))
  }

  test("Generate all configurations") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        GroupNode(1, None, List(
          OptNode("a", "a", Nil),
          OptNode("b", "b", List(
            GroupNode(1, None, List(
              OptNode("c", "c", Nil),
              OptNode("d", "d", Nil)
            ))
          ))
        ))
      )), Nil
    )

    val cnf = CNFSemantics.mkCNF(fm)
    
    val sat = new SATBuilder(cnf, fm.maxVar)

    val mi = new ModelIterator(sat.solver)
    //while (mi.isSatisfiable) {
      // println(Arrays.toString(mi.model()))
    // }
  }

  test("Generate all configurations (2)") {
    val fm = FeatureModel(
      RootNode("r", "r", List(
        GroupNode(1, None, List(
          OptNode("a", "a", Nil),
          OptNode("b", "b", Nil)
        )),
        OptNode("c", "c", Nil)
      )),
    List(Constraint("c1", "r" imp ("b" | "c")))
    )

    val cnf = CNFSemantics.mkCNF(fm)

    val sat = new SATBuilder(cnf, fm.maxVar)

    val mi = new ModelIterator(sat.solver)
    while (mi.isSatisfiable) {
      println(Arrays.toString(mi.model()))
    }
  }







//  {
//    // Parse and create CNF for splot models
//    val dir = new File(getClass.getResource("../../splot/").toURI)
//    val models = dir.listFiles(new FileFilter() {
//      def accept(f: File) = f.getName endsWith (".xml")
//    })
//
//    for (model <- models) {
//      test("Creating CNF for: %s".format(model.getName)) {
//        val fm = SXFMParser.parseFile(model.getCanonicalPath)
//        CNFSemantics.mkCNF(fm)
//      }
//    }
//  }

}
