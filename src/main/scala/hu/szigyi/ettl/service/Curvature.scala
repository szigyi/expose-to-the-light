package hu.szigyi.ettl.service

import hu.szigyi.ettl.util.ShutterSpeedMap._

import scala.concurrent.duration._

object Curvature {

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

  val settings: Seq[CurvedSetting] = Seq(
    CurvedSetting(20.0.minutes, 15d, 1600, 2.8),
    //  SettingsWithTime(15.0.minutes, 13, 1600, 2.8),
    //  SettingsWithTime(15.0.minutes, 10, 1600, 2.8),
    CurvedSetting(10.0.minutes, 8d, 800, 2.8),
    CurvedSetting(8.0.minutes, 6d, 800, 2.8),
    CurvedSetting(5.0.minutes, 5d, 800, 2.8),
    CurvedSetting(4.0.minutes, 3.2d, 800, 2.8),
    //  SettingsWithTime(4.0.minutes, 2.5d, 800, 2.8),
    //  SettingsWithTime(4.0.minutes, 2d, 800, 2.8),
    //  SettingsWithTime(4.0.minutes,  1.6d, 800, 2.8),
    CurvedSetting(4.0.minutes, 1.3d, 400, 2.8),
    CurvedSetting(3.0.minutes, 1d, 400, 2.8),
    CurvedSetting(3.0.minutes, 0.8d, 400, 2.8),
    CurvedSetting(3.0.minutes, 0.6d, 400, 2.8),
    CurvedSetting(3.0.minutes, 0.5d, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  0.4d, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  0.3d, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  1d/4d, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  1d/5d, 400, 2.8),
    CurvedSetting(3.0.minutes, 1d / 6d, 200, 2.8),
    CurvedSetting(3.0.minutes, 1d / 8d, 200, 2.8),
    CurvedSetting(3.0.minutes, 1d / 10d, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1d/13d, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1d/15d, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1d/20d, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1d/25d, 200, 2.8),
    CurvedSetting(3.0.minutes, 1d / 30d, 100, 2.8),
    CurvedSetting(3.0.minutes, 1d / 40d, 100, 2.8),
    CurvedSetting(3.0.minutes, 1d / 50d, 100, 2.8),
    CurvedSetting(3.0.minutes, 1d / 60d, 100, 2.8),
    CurvedSetting(4.0.minutes, 1d / 80d, 100, 2.8),
    CurvedSetting(4.0.minutes, 1d / 100d, 100, 2.8),
    CurvedSetting(4.0.minutes, 1d / 125d, 100, 2.8),
    CurvedSetting(4.0.minutes, 1d / 160d, 100, 2.8),
    CurvedSetting(4.0.minutes, 1d / 200d, 100, 2.8),
    CurvedSetting(5.0.minutes, 1d / 250d, 100, 2.8),
    CurvedSetting(5.0.minutes, 1d / 320d, 100, 2.8),
    CurvedSetting(5.0.minutes, 1d / 400d, 100, 2.8),
    CurvedSetting(5.0.minutes, 1d / 500d, 100, 2.8),
    CurvedSetting(5.0.minutes, 1d / 640d, 100, 2.8),
    CurvedSetting(10.0.minutes, 1d / 800d, 100, 2.8),
    CurvedSetting(10.0.minutes, 1d / 1000d, 100, 2.8),
    CurvedSetting(10.0.minutes, 1d / 1250d, 100, 2.8),
    CurvedSetting(10.0.minutes, 1d / 1600d, 100, 2.8),
    CurvedSetting(10.0.minutes, 1d / 2000d, 100, 2.8),
    CurvedSetting(15.0.minutes, 1d / 2500d, 100, 2.8),
    CurvedSetting(15.0.minutes, 1d / 3200d, 100, 2.8),
    CurvedSetting(15.0.minutes, 1d / 4000d, 100, 2.8),
    CurvedSetting(15.0.minutes, 1d / 5000d, 100, 2.8),
    CurvedSetting(20.0.minutes, 1d / 6400d, 100, 2.8),
    CurvedSetting(20.0.minutes, 1d / 8000d, 100, 2.8)
  )
}
