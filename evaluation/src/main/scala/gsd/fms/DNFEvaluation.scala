package gsd.fms

import gsd.fms.dnf._
import dk.itu.fms.DNFAdapter
import java.text.DecimalFormat
import java.io.PrintStream

object DNFEvaluation {
  
  def measure(f: => Unit): Long = {
    val begin = System.currentTimeMillis()
    f
    val end = System.currentTimeMillis()
    end - begin
  }

   val formatter = new DecimalFormat("#.###")

  def asSecondsString(time: Long): String =
    formatter.format(time.toFloat / 1000)

  def evaluateImplications(dnf: DNF, size: Int): Long = {
    val builder = new DNFImplBuilder(dnf, size)
    measure {
      builder.implications
    }
  }

  def evaluateOrGroups(dnf: DNF, size: Int)(hierarchy: Map[Int, Int]): Long = {
    val siblings: Map[Int, Set[Int]] =
      hierarchy.groupBy(_._2).mapValues(_.keys.toSet)

    measure {
      siblings flatMap {
        case (parent, children) =>
          DNFAdapter.orGroups(dnf, parent, children)
      }
    }
  }
  
  def evaluateOrGroupsWithNoHierarchy(dnf: DNF,  size: Int): Long = {
    val max = dnf.maxVar
    measure {
      for (i <- 1 to max) DNFAdapter.orGroups(dnf, i)
    }
  }

  /**
   * @param args args(0) is the file to write the output too
   */
  def main(args: Array[String]) {
    val out =  System.out

    // TODO process a bunch of CXTs

    val cxt = CXTParser.parse(args(0))
    val dnf = cxt.toDNF
    
    val implTime = evaluateImplications(dnf, cxt.maxVar)
    val groupTime = evaluateOrGroupsWithNoHierarchy(dnf, cxt.maxVar)
    
    out.println("%s,implications".format(asSecondsString(implTime)))
    out.println("%s,or-groups".format(asSecondsString(groupTime)))
  }

}
