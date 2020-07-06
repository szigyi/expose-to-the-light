package hu.szigyi.ettl.service

import java.util.concurrent.TimeUnit
import java.time.{Duration => JavaDuration}

import scala.concurrent.duration._

object Curvature {

  case class SettingsWithTime(duration: Duration, shutterSpeed: Double, iso: Int)

  object SettingsWithTime {
    def apply(duration: JavaDuration, shutterSpeed: Double, iso: Int): SettingsWithTime =
      new SettingsWithTime(FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS), shutterSpeed, iso)
  }

  val settings: Seq[SettingsWithTime] = Seq(
    SettingsWithTime(20.0.minutes, 15, 1600),
    //  SettingsWithTime(15.0.minutes, 13, 1600),
    //  SettingsWithTime(15.0.minutes, 10, 1600),
    SettingsWithTime(10.0.minutes, 8, 800),
    SettingsWithTime(8.0.minutes, 6, 800),
    SettingsWithTime(5.0.minutes, 5, 800),
    SettingsWithTime(4.0.minutes, 3.2, 800),
    //  SettingsWithTime(4.0.minutes, 2.5, 800),
    //  SettingsWithTime(4.0.minutes, 2, 800),
    //  SettingsWithTime(4.0.minutes,  1.6, 800),
    SettingsWithTime(4.0.minutes, 1.3, 400),
    SettingsWithTime(3.0.minutes, 1, 400),
    SettingsWithTime(3.0.minutes, 0.8, 400),
    SettingsWithTime(3.0.minutes, 0.6, 400),
    SettingsWithTime(3.0.minutes, 0.5, 400),
    //  SettingsWithTime(3.0.minutes,  0.4, 400),
    //  SettingsWithTime(3.0.minutes,  0.3, 400),
    //  SettingsWithTime(3.0.minutes,  1/4, 400),
    //  SettingsWithTime(3.0.minutes,  1/5, 400),
    SettingsWithTime(3.0.minutes, 1 / 6, 200),
    SettingsWithTime(3.0.minutes, 1 / 8, 200),
    SettingsWithTime(3.0.minutes, 1 / 10, 200),
    //  SettingsWithTime(3.0.minutes,  1/13, 200),
    //  SettingsWithTime(3.0.minutes,  1/15, 200),
    //  SettingsWithTime(3.0.minutes,  1/20, 200),
    //  SettingsWithTime(3.0.minutes,  1/25, 200),
    SettingsWithTime(3.0.minutes, 1 / 30, 100),
    SettingsWithTime(3.0.minutes, 1 / 40, 100),
    SettingsWithTime(3.0.minutes, 1 / 50, 100),
    SettingsWithTime(3.0.minutes, 1 / 60, 100),
    SettingsWithTime(4.0.minutes, 1 / 80, 100),
    SettingsWithTime(4.0.minutes, 1 / 100, 100),
    SettingsWithTime(4.0.minutes, 1 / 125, 100),
    SettingsWithTime(4.0.minutes, 1 / 160, 100),
    SettingsWithTime(4.0.minutes, 1 / 200, 100),
    SettingsWithTime(5.0.minutes, 1 / 250, 100),
    SettingsWithTime(5.0.minutes, 1 / 320, 100),
    SettingsWithTime(5.0.minutes, 1 / 400, 100),
    SettingsWithTime(5.0.minutes, 1 / 500, 100),
    SettingsWithTime(5.0.minutes, 1 / 640, 100),
    SettingsWithTime(10.0.minutes, 1 / 800, 100),
    SettingsWithTime(10.0.minutes, 1 / 1000, 100),
    SettingsWithTime(10.0.minutes, 1 / 1250, 100),
    SettingsWithTime(10.0.minutes, 1 / 1600, 100),
    SettingsWithTime(10.0.minutes, 1 / 2000, 100),
    SettingsWithTime(15.0.minutes, 1 / 2500, 100),
    SettingsWithTime(15.0.minutes, 1 / 3200, 100),
    SettingsWithTime(15.0.minutes, 1 / 4000, 100),
    SettingsWithTime(15.0.minutes, 1 / 5000, 100),
    SettingsWithTime(20.0.minutes, 1 / 6400, 100),
    SettingsWithTime(20.0.minutes, 1 / 8000, 100)
  )
}
