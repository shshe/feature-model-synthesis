package gsd.fms

import scala.util.parsing.combinator.{ImplicitConversions, PackratParsers, JavaTokenParsers}


trait ExprParser extends JavaTokenParsers with PackratParsers with ImplicitConversions {

  lazy val expr: PackratParser[Expr] =
    (impExpr ~ (("biimp" | "<->") ~> expr)) ^^ Biimp | impExpr

  lazy val impExpr: PackratParser[Expr] =
    (orExpr ~ (("imp" | "->") ~> impExpr)) ^^ Imp | orExpr

  lazy val orExpr: PackratParser[Expr] =
    (andExpr ~ (("or" | "|") ~> orExpr)) ^^ Or | andExpr

  lazy val andExpr: PackratParser[Expr] =
    (unaryExpr ~ (("and" | "&") ~> andExpr)) ^^ And | unaryExpr

  lazy val unaryExpr: PackratParser[Expr] =
    "~" ~> primaryExpr ^^ {
      case x => Not(x)
    } | primaryExpr

  lazy val primaryExpr: PackratParser[Expr] =
    "(" ~> expr <~ ")" | ident ^^ Id

  def succ[A](p: ParseResult[A]) = p match {
    case Success(res, _) => res
    case x => sys.error(x.toString)
  }

  def parseExpr(str: String): Expr =
    succ(parseAll(expr, str))

}

