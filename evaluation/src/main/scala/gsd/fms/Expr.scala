package gsd.fms

abstract class Expr

case class Id(id: String) extends Expr
case class And(l: Expr, r: Expr) extends Expr
case class Or (l: Expr, r: Expr) extends Expr
case class Imp(l: Expr, r: Expr) extends Expr
case class Biimp(l: Expr, r: Expr) extends Expr

case class Not(e: Expr) extends Expr

