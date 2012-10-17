package gsd.fms

import org.scalatest.FunSuite
import java.io.File

class SXFMParserTest extends FunSuite {

  import SXFMParser._

  implicit def toId(s: String) = Id(s)

  test("Count Leading Tabs") {
    expect(0)(countLeadingTabs(""))
    expect(1)(countLeadingTabs("\t"))
    expect(2)(countLeadingTabs("\t\t"))
    expect(2)(countLeadingTabs("\t\tSomething Here"))
    expect(3)(countLeadingTabs("\t\t\tSomething Here\t"))
    expect(3)(countLeadingTabs("        \t\t\t"))
  }

  test("Basic SXFM input") {
    val result = parseXML {
     <feature_model>
      <meta>
        <data name="creator">Steven She</data>
      </meta>
        <feature_tree>
          {
            ":r root (root)\n" +
            "\t:o o1 (o1)\n" +
            "\t:m m1\n" +
            "\t\t:g [1,*]\n" +
            "\t\t\t: a (a)\n" +
            "\t\t\t: b (b)\n" +
            "\t\t\t\t:o b1\n" +
            "\t:o o2 (o2)\n" +
            "\t\t:m m2\n"
          }
        </feature_tree>
       <constraints>
         {
         "c1: ~o2 or b\n"
         }
       </constraints>
     </feature_model>
    }

    val expectedTree =
      RootNode("root", "root",List(
        OptNode("o1", "o1", List()),
        MandNode("m1", "m1", List(
          GroupNode(1, None, List(
            OptNode("a", "a", List()),
            OptNode("b", "b", List(
              OptNode("b1", "b1", List())
            ))
         ))
        )),
        OptNode("o2", "o2", List(
          MandNode("m2", "m2", List())
        ))
      ))

    val expectedConstraints =
      List(Constraint("c1", !"o2" | "b" ))

    expect(expectedTree)(result.root)
    expect(expectedConstraints)(result.constraints)
  }
  
  test("eshop.sxfm.xml") {
    val filename =
      new File(getClass.getResource("../../eshop.sxfm.xml").toURI).getCanonicalPath

    parseFile(filename)
  }

}
