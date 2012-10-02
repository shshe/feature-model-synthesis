package gsd.sat

import org.scalatest.FunSuite

class MutexBuilderTest extends FunSuite {

  test("empty builder is initialized") {
    new SATBuilder(List(), 1) with MutexBuilder
  }
  
  test("mutex is found") {
    val b = new SATBuilder(List(List(-1, -2)), 2) with MutexBuilder
    assert(b.mutex(1,2))
    assert(b.mutex(2,1))
  }

  test("invalid mutex is not found") {
    val b = new SATBuilder(List(List(-1, 2)), 2) with MutexBuilder
    assert(!b.mutex(1,2))
    assert(!b.mutex(2,1))
  }

  test("multiple mutexes are found") {
    val b = new SATBuilder(List(List(-1, -2), List(-2, -3)), 3) with MutexBuilder
    assert(b.mutex(1,2))
    assert(b.mutex(2,1))
    assert(b.mutex(2,3))
    assert(b.mutex(3,2))
    assert(!b.mutex(1,3))
    assert(!b.mutex(3,1))
  }

}
