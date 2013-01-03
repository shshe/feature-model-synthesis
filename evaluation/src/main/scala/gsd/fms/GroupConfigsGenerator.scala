package gsd.fms

import java.io.{PrintStream, File}

import gsd.fms.dnf._

object GroupConfigsGenerator {
  
  def genGroupConfigs(numGroups: Int, totalGroups: Int, minSize: Int = 3): List[Set[Int]] = {
    (for (i <- 0 until numGroups) yield {
      val memLow: Int = numGroups + minSize * i + 1
      for (j <- memLow until (memLow + minSize)) yield Set(i+1, j)
    }).flatten[Set[Int]].toList
  }

  def genExponentialConfigs(mandSize: Int, size: Int = 2): DNF =
    ((for (i <- 0 until size) yield
      (i*mandSize + 1 until i*mandSize + mandSize + 1).toSet) map
      (_ + (size*mandSize + 1))).toSet

  
  def main(args: Array[String]) {
    val maxGroups = 40
    val size = 2
    val maxVar = maxGroups * (size+1)


     for (numGroups <- 1 to maxGroups) {
       val f = new File("groups/group-" + numGroups + ".cxt")
       val out = new PrintStream(f)
       // val result = genGroupConfigs(numGroups, maxGroups)
       val result = genExponentialConfigs(numGroups, size)
       val negated = result.negateUnboundedVars(maxVar)
       val cxt = Context.fromDNF(negated)
       out.println(cxt)
       out.close()
    }
  }

}
