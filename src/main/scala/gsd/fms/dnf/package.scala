package gsd.fms

package object dnf {

  type Term = Set[Int]
  type DNF = Iterable[Term]

  implicit def toRichDNF(dnf: DNF) = new {

    def addSyntheticRoot: DNF = {
      val newVar = maxVar
      dnf map (_ + maxVar)
    }

    /**
     * assumes DNF contains at least one term
     */
    def maxVar: Int =
      (dnf map (_ map (math.abs(_)) max)).max

    def negateUnboundedVars(maxVar: Int = this.maxVar): DNF =
      dnf map (x => x ++ (((1 to maxVar).toSet -- x) map (-_)))

    def retainVars(vars: Set[Int]): DNF =
      dnf map { term =>
        term filter (vars contains math.abs(_))
      } filterNot (_.isEmpty)
  }

}