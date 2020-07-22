package hu.szigyi.ettl.service

import hu.szigyi.ettl.util.ShutterSpeedMap._

import scala.concurrent.duration._

object KeyFrameService {

  case class CurvedSetting(duration: Duration,
                           shutterSpeed: Double,
                           shutterSpeedString: String,
                           iso: Int,
                           aperture: Double)

  object CurvedSetting {
    def apply(duration: Duration, shutterSpeed: Double, iso: Int, aperture: Double): CurvedSetting = {
      toShutterSpeed(shutterSpeed) match {
        case Some(shutterSpeedString) =>
          new CurvedSetting(duration, shutterSpeed, shutterSpeedString, iso, aperture)
        case None =>
          throw new Exception(s"Shutter speed is not valid. String version of it is not found: $shutterSpeed")
      }

    }
  }
}
