package gsd.fms

import scala.util.parsing.combinator.{ImplicitConversions, JavaTokenParsers}
import scala.collection.mutable
import scala.util.parsing.input.PagedSeqReader
import scala.collection.immutable.PagedSeq

object FMParser extends JavaTokenParsers with ImplicitConversions {

  private abstract sealed class Child
  private case class Feature(name: String, isMandatory: Boolean) extends Child
  private case class Group(members: List[String], groupType: GroupType) extends Child
  private case class Production(name: String, children: List[Child])


  private abstract sealed class GroupType
  private case object XorGroup extends GroupType
  private case object OrGroup extends GroupType
  private case object MutexGroup extends GroupType


  lazy val fm: Parser[FeatureModel] = {
    ((production | expr) <~ ";").* ^^ {
      case elements =>
        val productions: Iterable[Production] = elements.collect {
          case x:Production => x
        }
        val exprs: Iterable[Expr] = elements.collect {
          case x:Expr => x
        }

        val prodMap = (productions map (p => p.name -> p)).toMap withDefault {
          name => Production(name, Nil)
        }
        val optMap = new mutable.HashMap[String, Boolean]() withDefaultValue(true)

        productions foreach {
          case Production(name, children) =>
            children foreach {
              case Feature(featureName, false) =>
                optMap += featureName -> false

              case Feature(featureName, true) =>
                optMap += featureName -> true

              case Group(members, groupType) =>
                // Do nothing
            }
        }

        def processProduction(p: Production): NamedNode = p match {
          case Production(name, children) if optMap(name) =>
            OptNode(name, name, children map processChild)

          case Production(name, children)  =>
            MandNode(name, name, children map processChild)
        }

        def processChild(c: Child): Node = c match {
          case Feature(name, _) =>
            processProduction(prodMap(name))
          case Group(members, groupType) =>
            val (min, max) = groupType match {
              case XorGroup => (1, Some(1))
              case OrGroup => (1, None)
              case MutexGroup => (0, Some(1))
            }
            GroupNode(min, max, members map prodMap map processProduction collect {
              case x: OptNode => x
            })
        }

        val namedRoot = processProduction(productions.head)
        val root = RootNode(namedRoot.id, namedRoot.name, namedRoot.children)

      FeatureModel("FM", root,
        exprs.toList zip ((1 to exprs.size) map
          (_.toString)) map
          (_.swap) map { case (x,y) => Constraint(x,y) })
    }
  }

  private lazy val production: Parser[Production] =
    (ident <~ ":") ~ (feature | group).+ ^^ Production

  private lazy val feature: Parser[Feature] =
    ident ~ (("?".?) ^^ (_.isDefined)) ^^ Feature

  private lazy val group: Parser[Group] =
    (("(" ~> ident ~ ("|" ~> ident).+) <~ ")") ~
      (("+" ^^^ OrGroup | "?" ^^^ MutexGroup).? ^^ {
        case Some(x) => x
        case None => XorGroup
      }) ^^ {
      case first~rest~groupType => Group(first :: rest, groupType)
    }

  lazy val expr: Parser[Expr] = or_expr

  lazy val or_expr: Parser[Expr] = (and_expr ~ ("|" ~> and_expr).+) ^^ {
    case left~rights => (left::rights) reduceLeft Or
  } | and_expr

  lazy val and_expr: Parser[Expr] = (impl_expr ~ ("&" ~> impl_expr).+) ^^ {
    case left~rights => (left::rights) reduceLeft And
  } | impl_expr

  lazy val impl_expr: Parser[Expr] = (biimpl_expr ~ ("->" ~> biimpl_expr).+) ^^ {
    case left~rights => (left::rights) reduceLeft Imp
  } | biimpl_expr

  lazy val biimpl_expr: Parser[Expr] = (unary_expr ~ ("<->" ~> unary_expr).+) ^^ {
    case left~rights => (left::rights) reduceLeft Biimp
  } | unary_expr

  lazy val unary_expr: Parser[Expr] = ("!" ~> unary_expr) ^^ Not | primary

  lazy val primary: Parser[Expr] = ident ^^ Id | (("(" ~> expr) <~ ")")


  def parse(str: String): FeatureModel = parseAll(fm, str) match {
    case Success(result,_) => result
    case x => sys.error(x.toString)
  }

  def parse(pseq: PagedSeq[Char]): FeatureModel =
    parseAll(fm, new PagedSeqReader(pseq)) match {
      case Success(result,_) => result
      case x => sys.error(x.toString)
    }


  def main(args: Array[String]) {
    val fm = parse(PagedSeq fromFile args(0))
    println(fm)
  }

}
