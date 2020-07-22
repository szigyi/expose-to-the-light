package hu.szigyi.ettl.service

import java.time.ZonedDateTime
import java.time.{Duration => JDuration}

import hu.szigyi.ettl.client.influx.InfluxDomain.KeyFrame
import hu.szigyi.ettl.service.KeyFrameService.CurvedSetting

import scala.concurrent.duration.Duration

object Scale {
  case class ScaledSetting(time: ZonedDateTime,
                           shutterSpeed: Double,
                           shutterSpeedString: String,
                           iso: Int,
                           aperture: Double)

  implicit class RichDateTime(val dateTime: ZonedDateTime) extends AnyVal {
    def +(duration: Duration): ZonedDateTime = {
      dateTime.plus(JDuration.ofMillis(duration.toMillis))
    }
  }

  val darknessBeforeSunset = 3

  def scale(curvature: Seq[CurvedSetting], sunset: ZonedDateTime): Seq[ScaledSetting] = {
    var darknessBegins = sunset.minusHours(darknessBeforeSunset)
    curvature.map(s => {
      darknessBegins += s.duration
      ScaledSetting(darknessBegins, s.shutterSpeed, s.shutterSpeedString, s.iso, s.aperture)
    })
  }

  def scaleKeyFrames(keyFrames: Seq[KeyFrame], sunset: ZonedDateTime): Seq[ScaledSetting] = {
    var darknessBegins = sunset.minusHours(darknessBeforeSunset)
    keyFrames.map(s => {
      darknessBegins += s.duration
      ScaledSetting(darknessBegins, s.shutterSpeed, s.shutterSpeedString, s.iso, s.aperture)
    })
  }
}
