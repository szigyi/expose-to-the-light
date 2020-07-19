package hu.szigyi.ettl.service

object EvService {
//  case class EVSetting(shutterSpeed: Double, shutterSpeedString: String, iso: Int, aperture: Double, ev: Double)

  def ev(iso: Int, shutterSpeed: Double, aperture: Double): Double = {
    val ev100 = math.log(math.pow(aperture, 2) / shutterSpeed)
    if (iso > 100) ev100 + math.log(iso / 100)
    else ev100
  }
}
