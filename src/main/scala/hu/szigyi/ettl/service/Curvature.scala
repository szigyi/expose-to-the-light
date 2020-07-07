package hu.szigyi.ettl.service

import java.util.concurrent.TimeUnit
import java.time.{Duration => JavaDuration}

import scala.concurrent.duration._

object Curvature {

  case class CurvedSetting(duration: Duration, shutterSpeed: Double, iso: Int, aperture: Double)

  object CurvedSetting {
    def apply(duration: JavaDuration, shutterSpeed: Double, iso: Int, aperture: Double): CurvedSetting =
      new CurvedSetting(FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS), shutterSpeed, iso, aperture)
  }

  val settings: Seq[CurvedSetting] = Seq(
    CurvedSetting(20.0.minutes, 15, 1600, 2.8),
    //  SettingsWithTime(15.0.minutes, 13, 1600, 2.8),
    //  SettingsWithTime(15.0.minutes, 10, 1600, 2.8),
    CurvedSetting(10.0.minutes, 8, 800, 2.8),
    CurvedSetting(8.0.minutes, 6, 800, 2.8),
    CurvedSetting(5.0.minutes, 5, 800, 2.8),
    CurvedSetting(4.0.minutes, 3.2, 800, 2.8),
    //  SettingsWithTime(4.0.minutes, 2.5, 800, 2.8),
    //  SettingsWithTime(4.0.minutes, 2, 800, 2.8),
    //  SettingsWithTime(4.0.minutes,  1.6, 800, 2.8),
    CurvedSetting(4.0.minutes, 1.3, 400, 2.8),
    CurvedSetting(3.0.minutes, 1, 400, 2.8),
    CurvedSetting(3.0.minutes, 0.8, 400, 2.8),
    CurvedSetting(3.0.minutes, 0.6, 400, 2.8),
    CurvedSetting(3.0.minutes, 0.5, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  0.4, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  0.3, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  1/4, 400, 2.8),
    //  SettingsWithTime(3.0.minutes,  1/5, 400, 2.8),
    CurvedSetting(3.0.minutes, 1 / 6, 200, 2.8),
    CurvedSetting(3.0.minutes, 1 / 8, 200, 2.8),
    CurvedSetting(3.0.minutes, 1 / 10, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1/13, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1/15, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1/20, 200, 2.8),
    //  SettingsWithTime(3.0.minutes,  1/25, 200, 2.8),
    CurvedSetting(3.0.minutes, 1 / 30, 100, 2.8),
    CurvedSetting(3.0.minutes, 1 / 40, 100, 2.8),
    CurvedSetting(3.0.minutes, 1 / 50, 100, 2.8),
    CurvedSetting(3.0.minutes, 1 / 60, 100, 2.8),
    CurvedSetting(4.0.minutes, 1 / 80, 100, 2.8),
    CurvedSetting(4.0.minutes, 1 / 100, 100, 2.8),
    CurvedSetting(4.0.minutes, 1 / 125, 100, 2.8),
    CurvedSetting(4.0.minutes, 1 / 160, 100, 2.8),
    CurvedSetting(4.0.minutes, 1 / 200, 100, 2.8),
    CurvedSetting(5.0.minutes, 1 / 250, 100, 2.8),
    CurvedSetting(5.0.minutes, 1 / 320, 100, 2.8),
    CurvedSetting(5.0.minutes, 1 / 400, 100, 2.8),
    CurvedSetting(5.0.minutes, 1 / 500, 100, 2.8),
    CurvedSetting(5.0.minutes, 1 / 640, 100, 2.8),
    CurvedSetting(10.0.minutes, 1 / 800, 100, 2.8),
    CurvedSetting(10.0.minutes, 1 / 1000, 100, 2.8),
    CurvedSetting(10.0.minutes, 1 / 1250, 100, 2.8),
    CurvedSetting(10.0.minutes, 1 / 1600, 100, 2.8),
    CurvedSetting(10.0.minutes, 1 / 2000, 100, 2.8),
    CurvedSetting(15.0.minutes, 1 / 2500, 100, 2.8),
    CurvedSetting(15.0.minutes, 1 / 3200, 100, 2.8),
    CurvedSetting(15.0.minutes, 1 / 4000, 100, 2.8),
    CurvedSetting(15.0.minutes, 1 / 5000, 100, 2.8),
    CurvedSetting(20.0.minutes, 1 / 6400, 100, 2.8),
    CurvedSetting(20.0.minutes, 1 / 8000, 100, 2.8)
  )
}
