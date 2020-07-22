package hu.szigyi.ettl.api


import java.time.format.DateTimeFormatter

import cats.effect.IO
import hu.szigyi.ettl.api.ApiTool.ZonedDateTimeVar
import hu.szigyi.ettl.api.KeyFrameApi._
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.client.influx.InfluxDomain.KeyFrame
import hu.szigyi.ettl.service.Scale.ScaledSetting
import hu.szigyi.ettl.service.{EvService, Scale}
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class KeyFrameApi(influx: InfluxDbClient[IO]) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      influx.getKeyFrameIds.flatMap(Ok(_))

    case GET -> Root / id / ZonedDateTimeVar(sunset) =>
      for {
        keyFrames <- influx.getKeyFrames(id)
        scaled <- IO.pure(Scale.scaleKeyFrames(keyFrames, sunset))
        response <- Ok(scaled.map(toScaledModel))
      } yield response
  }
}

object KeyFrameApi {
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  case class CurvedModel(duration: Long, shutterSpeed: Double, iso: Int, ev: Double)
  case class ScaledModel(time: String, shutterSpeed: Double, iso: Int, ev: Double)

  def toCurvedModel(s: KeyFrame): CurvedModel =
    CurvedModel(s.duration.toNanos, s.shutterSpeed, s.iso, EvService.ev(s.iso, s.shutterSpeed, s.aperture))

  def toScaledModel(s: ScaledSetting): ScaledModel =
    ScaledModel(s.time.format(formatter), s.shutterSpeed, s.iso, EvService.ev(s.iso, s.shutterSpeed, s.aperture))
}
