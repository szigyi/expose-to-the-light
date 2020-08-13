package hu.szigyi.ettl.service

import java.time.ZonedDateTime
import java.time.{Duration => JDuration}

import hu.szigyi.ettl.client.influx.InfluxDomain.KeyFrameDomain
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

  def scale(curvature: Seq[CurvedSetting], startAt: ZonedDateTime): Seq[ScaledSetting] = {
    var timelapseBegins = startAt.minusNanos(0)
    curvature.map(s => {
      timelapseBegins += s.duration
      ScaledSetting(timelapseBegins, s.shutterSpeed, s.shutterSpeedString, s.iso, s.aperture)
    })
  }

  def scaleKeyFrames(keyFrames: Seq[KeyFrameDomain], startAt: ZonedDateTime): Seq[ScaledSetting] = {
    var timelapseBegins = startAt.minusNanos(0)
    keyFrames.map(s => {
      timelapseBegins += s.duration
      ScaledSetting(timelapseBegins, s.shutterSpeed, s.shutterSpeedString, s.iso, s.aperture)
    })
  }
}
