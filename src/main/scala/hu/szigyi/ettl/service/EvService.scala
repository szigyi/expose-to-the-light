package hu.szigyi.ettl.service

object EvService {

  def ev(iso: Int, shutterSpeed: Double, aperture: Double): Double = {
    val ev100 = math.log(math.pow(aperture, 2) / shutterSpeed)
    if (iso > 100) ev100 + math.log(iso / 100)
    else ev100
  }
}
