package gsd.fms

import net.sf.javabdd.{BDDFactory, BDD}


class BDDSemantics {

  val factory = BDDFactory.init("java", 10000000, 1000000)

  // adapted from BDD.java
  def allConfigs(in: BDD): Set[Int] = {
    var result: Set[Int] = Set()
    def rec(r: BDD, set: Array[Int]) {
      if (r.isZero) return
      else if (r.isOne) {
      // Do nothing
        result =
          (for (n <- 1 until set.length) yield
            if (set(n) == 2) factory.level2Var(n)
            else -factory.level2Var(n)
            ).toSet
      }
      else {
        set(factory.var2Level(r.`var`())) = 1;
        val rl: BDD = r.low()
        rec(rl, set)
        rl.free()

        set(factory.var2Level(r.`var`())) = 2;
        val rh: BDD = r.high()
        rec(rh, set)
        rh.free()

        set(factory.var2Level(r.`var`())) = 0
      }
    }
    val set = new Array[Int](factory.varNum())
    rec(in, set)
    result
  }

  def domain(fm: FeatureModel): BDD =
    (fm.vars map (factory.ithVar)).foldLeft(factory.one())(_.andWith(_))

  def allConfigurations(fm: FeatureModel): Iterator[Set[Int]] =
    allConfigurations(fm, mkBDD(fm))

  def allConfigurations(fm: FeatureModel, bdd: BDD): Iterator[Set[Int]] = {
     val iter = bdd.iterator(domain(fm))
     val result = new Iterator[Set[Int]] {
       def hasNext = iter.hasNext
       def next = {
        val c = iter.next().asInstanceOf[BDD]
         //c.printSet();
         allConfigs(c)
       }
     }
    bdd.free()
    result
   }


  def mkBDD(fm: FeatureModel): BDD = {
    factory.setVarNum(fm.vars.size + 1)

    val idMap = fm.idMap

    implicit val nodeCountBDDOrdering =
      Ordering.by[BDD, Int]{ x: BDD => x.nodeCount }

    val q = new collection.mutable.PriorityQueue[BDD]()

    def ithVar(id: String)  = factory.ithVar(idMap(id))
    def nithVar(id: String) = factory.nithVar(idMap(id))

    def mkHierarchyClauses(parentId: String)(n: Node): List[BDD] = n match {
      case OptNode(id, _, cs) =>
        (ithVar(id) impWith ithVar(parentId)) :: (cs flatMap mkHierarchyClauses(id))

      case MandNode(id, _, cs) =>
        (ithVar(id) biimpWith ithVar(parentId)) :: (cs flatMap mkHierarchyClauses(id))

      case RootNode(_, _, _) =>
        sys.error("A root node should never occur in the middle of the tree.")

      case GroupNode(_, _, cs) =>
        (cs flatMap mkHierarchyClauses(parentId))
    }
    
    def mkOrClauses(parentId: String)(n: Node): List[BDD] = n match {
      case OptNode(id, _, cs) => cs flatMap mkOrClauses(id)
      case MandNode(id, _, cs) => cs flatMap mkOrClauses(id)
      case RootNode(_, _, _) =>
        sys.error("A root node should never occur in the middle of the tree.")
        
      case GroupNode(_, _, cs) =>
        val memberBdd = (cs map (_.id) map ithVar).foldLeft(factory.zero())(_.orWith(_))
        (ithVar(parentId) impWith memberBdd) :: (cs flatMap mkOrClauses(parentId))
    }

    def mkExcludeClauses(parentId: String)(n: Node): List[BDD] = n match {
      case OptNode(id, _, cs) => cs flatMap mkExcludeClauses(id)
      case MandNode(id, _, cs) => cs flatMap mkExcludeClauses(id)
      case RootNode(_, _, _) =>
        sys.error("A root node should never occur in the middle of the tree.")

      case GroupNode(1, None, cs) => (cs flatMap mkExcludeClauses(parentId))

      // xor-group
      case GroupNode(1, Some(1), cs) =>
        val combos = (cs map (_.id)).toList.combinations(2).toTraversable
        (combos map {
          case List(x, y) => ithVar(x) impWith nithVar(y)
        }).toList :::
          (cs flatMap mkExcludeClauses(parentId))

      case GroupNode(min, Some(max), cs) =>
        sys.error("Group with cardinality %d..%d is unsupported".format(min, max))
    }

    def mkExpression(e: Expr): BDD = e match {
      case Id(id: String) => ithVar(id)
      case Not(e: Expr) => {
        val f = mkExpression(e)
        val result = f.not()
        f.free()
        result
      }
      case And(l: Expr, r: Expr) => mkExpression(l) andWith mkExpression(r)
      case Or(l: Expr, r: Expr) => mkExpression(l) orWith mkExpression(r)
      case Imp(l: Expr, r: Expr) => mkExpression(l) impWith mkExpression(r)
      case Biimp(l: Expr, r: Expr) => mkExpression(l) biimpWith mkExpression(r)
    }

    val hierarchyClauses = fm.root.children flatMap mkHierarchyClauses(fm.root.id)
    val orClauses = fm.root.children flatMap mkOrClauses(fm.root.id)
    val excludeClauses = fm.root.children flatMap mkExcludeClauses(fm.root.id)
    val rootClause = ithVar(fm.root.id)
    val ctcs = fm.constraints map {
      case Constraint(_, e) => mkExpression(e)
    }

    q ++= hierarchyClauses
    q ++= orClauses
    q ++= excludeClauses
    q ++= ctcs
    q += rootClause

    q.foldLeft(factory.one())(_.andWith(_))
  }

}
