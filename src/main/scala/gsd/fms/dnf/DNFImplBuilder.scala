//package gsd.fms.dnf
//
//import gsd.graph.DirectedGraph
//
//class DNFImplBuilder(val dnf: DNF, val size: Int) {
//
//  // false if there exists a term that contains i && !j
//  // true if forall terms !i || j
//  def implications: Seq[(Int, Int)] = {
//    val results = new collection.mutable.ListBuffer[(Int, Int)]
//    for (i <- 1 to size) {
//      for (j <- 1 to size) {
//        println("Working on " + i + "," + j)
//        dnf forall { term => term.exists(x => x == -i || x == j) }
//      }
//    }
//    results
//  }
//
//  def implication(i: Int, j: Int): Boolean =
//    dnf forall { term => term.exists(x => x == -i || x == j) }
//
//  lazy val implicationGraph: DirectedGraph[Int] =
//}

