package gsd.fms

object CNFSemantics {

  import gsd.fms.sat._
  
  def mkCNF(fm: FeatureModel): CNF = {

    val idMap = fm.idMap
    
    def mkHierarchyClauses(parentId: String)(n: Node): List[Clause] = n match {
      case OptNode(id,_,cs) =>
        Set(-idMap(id), idMap(parentId)) :: (cs flatMap mkHierarchyClauses(id))

      case MandNode(id,_,cs) =>
        Set(-idMap(id), idMap(parentId)) ::
        Set(idMap(id), -idMap(parentId)) ::  (cs flatMap mkHierarchyClauses(id))

      case RootNode(_,_,_) =>
        sys.error("A root node should never occur in the middle of the tree.")

      case GroupNode(_,_,cs) =>
        (cs flatMap mkHierarchyClauses(parentId))
    }
    
    val hierarchyClauses = 
      fm.root.children flatMap mkHierarchyClauses(fm.root.id)
    
    def mkOrClauses(parentId: String)(n: Node): List[Clause] = n match {
      case OptNode(id,_,cs) =>
        cs flatMap mkOrClauses(id)
      case MandNode(id,_,cs) =>
        cs flatMap mkOrClauses(id)
      case RootNode(_,_,_) =>
        sys.error("A root node should never occur in the middle of the tree.")
      case GroupNode(_,_,cs) =>
        val memberIds = cs map (_.id) map (idMap(_))
        memberIds.toSet + (-idMap(parentId)) :: (cs flatMap  mkOrClauses(parentId))
    }
    
    val orClauses =
      fm.root.children flatMap mkOrClauses(fm.root.id)
    
    def mkExcludeClauses(parentId: String)(n: Node): List[Clause] = n match {
      case OptNode(id,_,cs) =>
        cs flatMap mkExcludeClauses(id)
        
      case MandNode(id,_,cs) =>
        cs flatMap mkExcludeClauses(id)
        
      case RootNode(_,_,_) =>
        sys.error("A root node should never occur in the middle of the tree.")

      // xor-group
      case GroupNode(1,Some(1),cs) =>
        val memberIds: List[Int] = cs map (_.id) map (idMap(_))
        val combos = memberIds.toList.combinations(2).toTraversable
        (combos map { case List(x,y) => Set(-x, -y) }).toList :::
          (cs flatMap mkExcludeClauses(parentId))

      case GroupNode(1,None, cs) =>
        (cs flatMap mkExcludeClauses(parentId))
        
      case GroupNode(min,Some(max),cs) =>
        sys.error("Group with cardinality %d..%d is unsupported".format(min, max))
    }

    val excludeClauses =
      fm.root.children flatMap mkExcludeClauses(fm.root.id)

    val crossTreeConstraints =
      fm.constraints flatMap {
        case Constraint(id, e) => CNFConverter.toCNF(idMap)(e)
      }

    hierarchyClauses ::: orClauses ::: excludeClauses ::: crossTreeConstraints
  }

}
