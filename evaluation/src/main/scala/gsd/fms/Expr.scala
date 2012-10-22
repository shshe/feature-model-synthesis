package gsd.fms

abstract class Expr(protected val precedence: Int) {
  override def toString: String

  def unary_! = Not(this)

  def &(other: Expr) = And(this, other)

  def |(other: Expr) = Or(this, other)

  def imp(other: Expr) = Imp(this, other)
}

case class Id(id: String) extends Expr(0) {
  override def toString = id
}

case class Not(e: Expr) extends Expr(1) {
  override def toString = "!(" + e + ")"
}

case class And(l: Expr, r: Expr) extends Expr(2) {
  override def toString = "(" + l + " & " + r + ")"
}

case class Or(l: Expr, r: Expr) extends Expr(3) {
  override def toString = "(" + l + " | " + r + ")"
}

case class Imp(l: Expr, r: Expr) extends Expr(4) {
  override def toString = "(" + l + " -> " + r + ")"
}

case class Biimp(l: Expr, r: Expr) extends Expr(5) {
  override def toString = "(" + l + " <-> " + r + ")"
}


