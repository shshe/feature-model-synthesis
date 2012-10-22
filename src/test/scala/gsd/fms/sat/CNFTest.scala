package gsd.fms.sat

import org.scalatest.FunSuite

import CNFBuilder._

class CNFTest extends FunSuite {

  test("clause difference") {
    expect(List(1, 2, 3, 4))(List(1, 2, 3, 4) diff List())
    expect(List(1, 3))(List(1, 2, 3, 4) diff List(2, 4))
    expect(List(-1, -3))(List(-1, -2, -3, -4) diff List(-2, -4))
  }

}
