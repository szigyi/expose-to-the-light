package hu.szigyi.ettl.util

object IsoMap {
  private val canonIso = Seq(
    100,
    125,
    160,
    200,
    250,
    320,
    400,
    500,
    640,
    800,
    1000,
    1250,
    1600,
    2000,
    2500,
    3200,
    4000,
    5000,
    6400,
    8000,
    10000,
    12800,
  )

  def getAll: Seq[Int] = canonIso
}
