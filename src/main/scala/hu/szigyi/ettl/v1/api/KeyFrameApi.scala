package hu.szigyi.ettl.v1.api


import java.time.format.DateTimeFormatter

import cats.effect.IO
import hu.szigyi.ettl.v1.api.ApiTool.ZonedDateTimeVar
import hu.szigyi.ettl.v1.api.KeyFrameApi._
import hu.szigyi.ettl.v1.influx.InfluxDbClient
import hu.szigyi.ettl.v1.influx.InfluxDomain.KeyFrameDomain
import hu.szigyi.ettl.v1.service.Scale.ScaledSetting
import hu.szigyi.ettl.v1.service.{EvService, Scale}
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class KeyFrameApi(influx: InfluxDbClient[IO]) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      influx.getKeyFrameIds.flatMap(Ok(_))

    case GET -> Root / id / ZonedDateTimeVar(startAt) =>
      for {
        keyFrames <- influx.getKeyFrames(id)
        scaled <- IO.pure(Scale.scaleKeyFrames(keyFrames, startAt))
        response <- Ok(scaled.map(toScaledModel))
      } yield response
  }
}

object KeyFrameApi {
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  case class CurvedModel(duration: Long, shutterSpeed: Double, iso: Int, ev: Double)
  case class ScaledModel(time: String, shutterSpeed: Double, iso: Int, ev: Double)

  def toCurvedModel(s: KeyFrameDomain): CurvedModel =
    CurvedModel(s.duration.toNanos, s.shutterSpeed, s.iso, EvService.ev(s.shutterSpeed, s.iso, s.aperture))

  def toScaledModel(s: ScaledSetting): ScaledModel =
    ScaledModel(s.time.format(formatter), s.shutterSpeed, s.iso, EvService.ev(s.shutterSpeed, s.iso, s.aperture))
}
