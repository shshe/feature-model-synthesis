package gsd.fms

import gsd.fms.dnf._
import gsd.graph.Graph

object ImplicationGraph {

  def mkImplicationGraph(dnf: DNF, size: Int): Graph[Int] =
    new DNFImplBuilder(dnf, size).mkImplicationGraph()
  
}
