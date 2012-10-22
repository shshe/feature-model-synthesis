package gsd.fms

import xml._

object SXFMParser {

  protected[fms] def countLeadingTabs(line: String): Int =
    line.dropWhile(_ == ' ').takeWhile(_ == '\t').length

  val featurePattern = """:(o|m|r)?\s+([^(]+)(?:\((\w+)\))?""".r
  val groupPattern = """:g\s+(?:\((\w+)\)\s+)?\[(\d),\s*([\d*])\]""".r

  // TODO constraints
  def parseXML(elem: Elem) = {
    val meta = elem \\ "meta" // FIXME not stored at the moment
    val featureTree = elem \\ "feature_tree"
    val constraints = elem \\ "constraints"

    FeatureModel(parseFeatureTree(featureTree.text),
      parseConstraints(constraints.text))
  }

  def parseConstraints(text: String) = {
    val lines = io.Source.fromString(text).getLines()

    (lines filterNot (_.trim == "") map
      ConstraintParser.parseConstraint).toList
  }

  def parseFeatureTree(text: String) = {
    // Buffered iterator for peek (i.e. head)
    val lines = io.Source.fromString(text).getLines().buffered

    def x: List[Node] =
      if (!lines.hasNext) Nil
      else {
        val s = lines.next()

        if (s.trim() == "") x
        else {
          val currLevel = countLeadingTabs(s)

          val children = new collection.mutable.ListBuffer[Node]
          while (lines.hasNext && countLeadingTabs(lines.head) > currLevel)
            children ++= x

          val siblings =
            if (countLeadingTabs(lines.head) < currLevel) Nil
            else x

          s.trim match {
            case featurePattern("o", name, id) =>
              OptNode(if (id == null) name.trim else id, name.trim, children.toList) :: siblings
            case featurePattern("m", name, id) =>
              MandNode(if (id == null) name.trim else id, name.trim, children.toList) :: siblings
            case featurePattern("r", name, id) =>
              RootNode(if (id == null) name.trim else id, name.trim, children.toList) :: siblings

            // Feature group members
            case featurePattern(null, name, id) =>
              OptNode(if (id == null) name.trim else id, name.trim, children.toList) :: siblings

            // Ignore the id
            case groupPattern(_, minCard, "*") =>
              val members = children collect {
                case n: OptNode => n
              }
              GroupNode(minCard.toInt, None, members.toList) :: siblings
            case groupPattern(_, minCard, maxCard) =>
              val members = children collect {
                case n: OptNode => n
              }
              GroupNode(minCard.toInt, Some(maxCard.toInt), members.toList) :: siblings
          }
        }
      }
    val result = x
    assert(result.head.isInstanceOf[RootNode])
    assert(result.size == 1)
    result.head.asInstanceOf[RootNode]
  }

  def parseFile(fileName: String) =
    parseXML(XML.loadFile(fileName))

}
