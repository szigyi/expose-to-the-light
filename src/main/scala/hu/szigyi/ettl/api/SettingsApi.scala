package hu.szigyi.ettl.api


import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import cats.effect.IO
import hu.szigyi.ettl.service.{Curvature, EvService, Scale}
import hu.szigyi.ettl.service.Curvature.CurvedSetting
import io.circe.Encoder
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import SettingsApi._
import hu.szigyi.ettl.api.ApiTool.{InstantVar, ZonedDateTimeVar}
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.client.influx.InfluxDomain.KeyFrame
import hu.szigyi.ettl.service.Scale.ScaledSetting

class SettingsApi(influx: InfluxDbClient[IO]) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "key-frames" =>
      influx.getKeyFrameIds.flatMap(Ok(_))

    case GET -> Root / "key-frames" / id / ZonedDateTimeVar(sunset) =>
      for {
        keyFrames <- influx.getKeyFrames(id)
        scaled <- IO.pure(Scale.scaleKeyFrames(keyFrames, sunset))
        response <- Ok(scaled.map(toScaledModel))
      } yield response

    case GET -> Root / "scaled" =>
      Ok(Scale.scale(Curvature.settings.reverse, ZonedDateTime.now()).map(toScaledModel))
  }
}

object SettingsApi {

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  case class CurvedModel(duration: Long, shutterSpeed: Double, iso: Int, ev: Double)
  case class ScaledModel(time: String, shutterSpeed: Double, iso: Int, ev: Double)

  def toCurvedModel(s: KeyFrame): CurvedModel =
    CurvedModel(s.duration.toNanos, s.shutterSpeed, s.iso, EvService.ev(s.iso, s.shutterSpeed, s.aperture))

  def toScaledModel(s: ScaledSetting): ScaledModel =
    ScaledModel(s.time.format(formatter), s.shutterSpeed, s.iso, EvService.ev(s.iso, s.shutterSpeed, s.aperture))


//  import java.time.{Duration => JavaDuration}
//
//  implicit val durationEncoder: Encoder[JavaDuration] = Encoder.encodeDuration
//  implicit val seqSettings: Encoder[SettingsWithTime] =
//    Encoder.forProduct3("duration", "shutterSpeed", "iso")(s => (toJavaDuration(s.duration), s.shutterSpeed, s.iso))
//
//  private def toJavaDuration(d: Duration): JavaDuration =
//    JavaDuration.ofNanos(d.toNanos)
}
