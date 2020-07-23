package hu.szigyi.ettl.service

object EvService {
  def ev(shutterSpeed: Double, iso: Int, aperture: Double): Double = {
    val ev100 = log2(math.pow(aperture, 2) / shutterSpeed)
    if (iso > 100) ev100 + log2(iso / 100)
    else ev100
  }

  private def log2(x: Double): Double = {
    val base10: Double = math.log10(x)
    base10 / math.log10(2)
  }
}
