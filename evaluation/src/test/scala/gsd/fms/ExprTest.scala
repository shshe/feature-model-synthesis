package gsd.fms

import org.scalatest.FunSuite

class ExprTest extends FunSuite {
  
  implicit def toId(s: String) = Id(s)

  test("precedence") {
    println("a" & "b" | "c" & "d")
    println("a" & "b" & "c" & "d")
    println("a" & ("b" | "c"))
  }
  
}
