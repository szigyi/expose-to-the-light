package hu.szigyi.ettl.util

object ApertureMap {
  private val canonApertures: Seq[Double] = Seq(
    2.8d,
    3.2d,
    3.5d,
    4.0d,
    4.5d,
    5.0d,
    5.6d,
    6.3d,
    7.1d,
    8.0d,
    9.0d,
    10d,
    11d,
    13d,
    14d,
    16d,
    18d,
    20d,
    22d
  )

  def getAll: Seq[Double] = canonApertures
}
