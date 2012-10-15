package gsd.fms

trait ConstraintParser extends ExprParser {
  
  lazy val constraint: PackratParser[Constraint] =
  (ident <~ ":") ~ expr ^^ Constraint

  def parseConstraint(str : String): Constraint =
    succ(parseAll(constraint, str))
}

object ConstraintParser extends ConstraintParser
