// package gsd.fms.sat
//
//
// // TODO: assumes no subsumed clauses or tautology clauses in the input
// /**
//  * An implicate D is a clause such that D is not a tautology and (phi -> D).
//  *
//  * @author Steven She
//  * @author Nele Andersen
//  */
// class PrimeImplicateGenerator(cnf: Set[Clause]) {
//
//   val complementsCache = new
//     collection.mutable.HashMap[Clause, collection.mutable.Set[Clause]]
//
//   protected[sat] def comps(ci: Clause): Set[Clause] =
//     cnf filter { cj =>
//       val cond1 = (lhs(ci) intersect rhs(cj)).size == 1
//       val cond2 = (rhs(ci) intersect lhs(cj)).size == 1
//
//       cond1 && !cond2 || !cond1 && cond2
//     }
//
//   // NOTE complements are mutable!!
//   protected[sat] def complements(ci: Clause): collection.mutable.Set[Clause] =
//     complementsCache.get(ci) match {
//
//       case Some(result) => result
//
//       case None =>
//         // FIXME ignoring this step for now
//         // (1) Avoid duplicate steps
//         // cj is not in complements(ci) if ci is in complements(cj)
//
//         //
//         // (2) preempt forward subsumption
//         //
//         val compsci = new collection.mutable.HashSet[Clause]()
//         compsci ++= comps(ci)
//         for {
//           cj <- compsci
//           ck <- compsci if ck != cj
//         } yield {
//           val c_ji: Set[Int] = cj diff ci
//           val c_ki: Set[Int] = ck diff ci
//           if (c_ji subsetOf c_ki) compsci -= c_ji
//         }
//         complementsCache += ci -> compsci
//         compsci
//   }
//
//   // Positive literals as a set of positive ints
//   protected def lhs(ci: Clause): Set[Int] =
//     (ci filter (_ > 0)).toSet
//
//   // Negative Literals as a set of positive ints
//   protected def rhs(ci: Clause): Set[Int] =
//     (ci filter (_ < 0) map (-_)).toSet
//
//
//   protected def cost(ci: Clause, cj: Clause): Int = {
//     val lhsi = ci.filter(_ > 0) count (cj.filter(_ > 0).contains)
//     val rhsj = ci.filter(_ < 0) count (cj.filter(_ < 0).contains)
//
//     ci.size + cj.size - lhsi - rhsj
//   }
//
//   // FIXME cache this
//   protected def costStar(ci: Clause): Int =  {
//     val cost_ci_cjs = for (cj <- complements(ci)) yield cost(ci, cj)
//     cost_ci_cjs.min
//   }
//
//   def pig() = {
//     implicit val minCostClauseOrdering = new Ordering[Clause] {
//       def compare(x: Clause, y: Clause) = costStar(x) - costStar(y)
//     }
//     val s = new collection.mutable.PriorityQueue[Clause]()
//     s ++= cnf filter (!complements(_).isEmpty)
//
//     // calculate the best complements for each clause (i.e., lowest cost)
//     def bestComplement(ci: Clause): Option[Clause] = complements(ci) match {
//       case Set() => None
//       case x => Some(x.minBy(costStar(_)))
//     }
//
//     // 4.3 if c_i's best (i.e. lowest cost) complement, c_j, has been deleted
//     // then recompute COST*(c_i), reschedule c_i, and goto 4.
//
//
//     def computePIs(s: List[Clause]): Unit = s match {
//       case Nil => cnf
//
//       // 4.1 remove c_i from s
//       // 4.2 if c_i has been deleted then goto 4
//       case ci::tail if cnf contains ci => computePIs(tail)
//       case ci::tail =>
//         // 4.3 if c_i's best (i.e., lowest cost) complement, c_j,
//
//         bestComplements(ci) match {
//           case None =>
//           case Some(cj) =>
//         }
//
//     }
//
//     // (4) if s is empty, then halt and return T
//
//
//     None
//   }
// }
