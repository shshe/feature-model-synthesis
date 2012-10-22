package gsd.fms

import org.kiama.rewriting.Rewriter._
import gsd.fms.sat._

object CNFConverter {

  val sDistributeRule: Strategy = oncetd {
    rule {
      case Or(And(x, y), z) => And(Or(x, z), Or(y, z))
      case Or(x, And(y, z)) => And(Or(x, y), Or(x, z))
    }
  }

  val sIffRule = everywheretd {
    rule {
      case Biimp(x, y) => (!x | y) & (!y | x)
    }
  }

  val sImpliesRule = everywheretd {
    rule {
      case Imp(x, y) => !x | y
    }
  }

  def splitConjunctions(in: Expr): List[Expr] = in match {
    case And(x, y) => splitConjunctions(x) ::: splitConjunctions(y)
    case e => List(e)
  }

  /**
   * Run until we reach a fixpoint.
   */
  def distribute(e: Expr): List[Expr] = {
    val result = rewrite(sDistributeRule)(e)
    if (result == e) splitConjunctions(result)
    else splitConjunctions(result) flatMap distribute
  }

  /**
   * @param idMap Maps identifiers in the expression to an integer
   */
  def toClause(idMap: Map[String, Int])(e: Expr): Clause = e match {
    case Not(Id(v)) => Set(-idMap(v))
    case Id(v) => Set(idMap(v))
    case Or(x, y) => toClause(idMap)(x) ++ toClause(idMap)(y)
    case _ => sys.error("Wrong format. Expression is not a clause: " + e)
  }

  def toCNF(idMap: Map[String, Int])(e: Expr) =
    splitConjunctions(rewrite(sIffRule <* sImpliesRule)(e)) flatMap
      (distribute(_)) map
      toClause(idMap)

}
