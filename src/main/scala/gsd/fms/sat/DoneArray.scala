package gsd.sat

protected trait DoneArray {

  /**
   * @return A vars x vars array where the 0th index is unused since
   * we ignore the 0th variable in the SAT solver.
   */
  def mkDoneArray(cutoff: Int, ignore: Iterable[Int]) = {
    val arr = Array.ofDim[Boolean](cutoff + 1, cutoff + 1)

    // Initialize self-tests to 'done'
    for (i <- 0 to cutoff)
      arr(i)(i) = true

    // Initialize variable 0 to 'done'
    for (i <- 0 to cutoff) {
      arr(0)(i) = true
      arr(i)(0) = true
    }

    // Initialize additional variables to ignore to 'done'
    for {
      i <- ignore if i <= cutoff
      j <- 0 to cutoff
    } {
      arr(i)(j) = true
      arr(j)(i) = true
    }
    arr
  }
}
