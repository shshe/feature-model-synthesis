/*
 * This file is part of the Linux Variability Modeling Tools (LVAT).
 *
 * Copyright (C) 2012 Steven She <shshe@gsd.uwaterloo.ca>
 *
 * LVAT is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * LVAT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LVAT.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package gsd.graph

import java.util.Scanner
import java.util.regex.Pattern
import java.io.File

/**
 * Graph parser that uses the scanner and regular expressions.
 */

/**
 * Should eventually be renamed to DirectedGraphParser.
 */
trait GraphParser {

  val vertexPat = Pattern.compile("""(\d+):\s*(\w+)""")
  val edgePat : Pattern

  protected type This <: Graph[String]
  def New(vs: Set[String], es: Iterable[Edge[String]]) : This

  def parseString(s: String)  = parse(new Scanner(s))
  def parseFile(file: String) = parse(new Scanner(new File(file)))

  def parse(in: Scanner): This = {
    val s = in.useDelimiter(";(\r?\n)*")

    val ids = new collection.mutable.HashMap[Int,String]
    while (s.hasNext(vertexPat)) {
      s.next(vertexPat)
      val id   = s.`match`.group(1).toInt
      val feat = s.`match`.group(2)
      ids += id -> feat
      //println(id + ": " + feat)
    }

    val edges = new collection.mutable.ListBuffer[Edge[String]]
    while (s.hasNext(edgePat)) {
      s.next(edgePat)
      val srcId = s.`match`.group(1).toInt
      val tarId = s.`match`.group(2).toInt
      edges += ((ids(srcId), ids(tarId)))
      //println(srcId + " ---> " + tarId)
    }

    s.close()
    New(ids.values.toSet, edges)
  }
}

object DirectedGraphParser extends GraphParser {
  override val edgePat = Pattern.compile("""(\d+)->(\d+)""")
  override type This = DirectedGraph[String]

  override def New(vs: Set[String], es: Iterable[Edge[String]]) =
    new DirectedGraph(vs, es)
}
