package gsd.fms

import java.io.{PrintStream, FileFilter, File}
import de.tud.iai.modelcompare.splot.CxtContextCreator
import de.tud.iai.modelcompare.fca.ManyObjectsFCAUtil
import colibri.lib.Concept
import java.util

import scala.collection.JavaConversions._

object ContextDensity {

  def mkContextDensity() {

  }

  def main(args: Array[String]) {
    val files = new File(args(0)).listFiles(new FileFilter() {
      def accept(f: File) = f.getName endsWith (".cxt")
    })

    val out = new PrintStream("rmes.csv")

    out.println("name,rmes,rmeslog")
    for (f <- files) {
      val creator = new CxtContextCreator
      creator.loadFile(f)
      val context = creator.createContext()
      val lattice = creator.createLattice(context)
      val fcaUtil = new ManyObjectsFCAUtil(lattice)

      val allAttributes = fcaUtil.castComparableSet(fcaUtil.getBottomConcept.getAttributes)
      val attributes = new util.HashSet[String](allAttributes)

      // dead features
      System.out.println("Dead features:")
      if (lattice.bottom().getObjects.isEmpty) {
        for (obj <- fcaUtil.getAddedAttributes(lattice.bottom())) {
          val deadFeature = obj.asInstanceOf[String]
          println(" * " + creator.getFeatureName(deadFeature))
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
          println(sorted.mkString("* {", ",", "}"))
          attributes.removeAll(sorted.subList(1, sorted.size()))
          newAttributeMap.put(concept, sorted.get(0))
        }
        else if (equivAttrs.size() == 1) {
          newAttributeMap.put(concept, equivAttrs.iterator().next())
        }
      }

      println("Number of living non-mandatory features: " + attributes.size())

      val allAttributeConcepts =  fcaUtil.getAllAttributeConcepts(attributes)
      val extentSizeSum: Double = (allAttributeConcepts map (_.getObjects.size)).sum

      val avgExtentSize = extentSizeSum / (allAttributeConcepts.size : Double)
//      val avgRelativeExtentSize = avgExtentSize / (context.getSizeObjects)
      val avgRelativeExtentSize = avgExtentSize / (fcaUtil.getTopConcept.getObjects.size : Double)
      val avgRelativeExtentSizeLog = math.log(avgExtentSize) / math.log(fcaUtil.getTopConcept.getObjects.size : Double)

      System.out.println("Average extent size: " + avgExtentSize)
      System.out.println("Average extent size (relative): " + avgRelativeExtentSize)

      out.println("\"" + f.getName + "\"," + avgRelativeExtentSize + "," + avgRelativeExtentSizeLog)
    }

    out.close()
  }

}
