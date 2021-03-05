package hu.szigyi.ettl.v1.api

import java.time.Instant

import cats.effect.IO
import hu.szigyi.ettl.v1.api.ApiTool.InstantVar
import hu.szigyi.ettl.v1.influx.InfluxDomain.{CapturedDomain, ToSetSettingDomain}
import hu.szigyi.ettl.v1.service.CameraService.CameraError
import hu.szigyi.ettl.v1.service.TimelapseService
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class TimelapseApi(tlService: TimelapseService) {
  import TimelapseApi._

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "captured" / InstantVar(from) =>
      Ok(tlService.getCapturedMetaData(from)
        .map(_.map(CapturedTaskResponse.apply)))

    case r@POST -> Root / "settings" =>
      (for {
        req <- r.as[StoreSettingsRequest]
        res <- tlService.storeSettings(req.keyFrameId, req.startAt).map(_.map(SettingResponse.apply))
      } yield res).flatMap(Ok(_))

    case r@POST -> Root / "settings" / "test" =>
      (for {
        req <- r.as[TestRequest]
        res <- tlService.storeTestSettings(req.keyFrameId).map(_.map(SettingResponse.apply))
      } yield res).flatMap(Ok(_))

    case r@POST -> Root / "capture" =>
      (for {
        req <- r.as[StoreCaptureRequest]
        res <- tlService.storeCaptureTicks(req.intervalSeconds, req.count, req.startAt)
      } yield res).flatMap(Ok(_))
  }
}

object TimelapseApi {
  implicit val cameraErrorEncoder: Encoder[CameraError] =
    Encoder.forProduct3("msg", "result", "suggestion")(c => (c.msg, c.result, c.suggestion))
  implicit val executedResponseEncoder: Encoder[CapturedTaskResponse] =
    Encoder.forProduct9("timestamp", "id", "test", "shutterSpeed", "iso",
      "aperture", "ev", "error", "suggestion")(t => (t.timestamp,
      t.id, t.test, t.shutterSpeed, t.iso, t.aperture, t.ev, t.error, t.suggestion))
  implicit val SettingResponseEncoder: Encoder[SettingResponse] =
    Encoder.forProduct7("timestamp", "id", "test", "shutterSpeed", "iso",
      "aperture", "ev")(t => (t.timestamp, t.id, t.test, t.shutterSpeed, t.iso, t.aperture, t.ev))

  case class SettingResponse(timestamp: Instant,
                             id: String,
                             test: Boolean,
                             shutterSpeed: Option[Double],
                             iso: Option[Int],
                             aperture: Option[Double],
                             ev: Option[Double])
  case class CapturedTaskResponse(timestamp: Instant,
                                  id: String,
                                  test: Boolean,
                                  shutterSpeed: Double,
                                  iso: Int,
                                  aperture: Double,
                                  ev: Double,
                                  error: Option[String],
                                  suggestion: Option[String])
  case class TestRequest(keyFrameId: String)
  case class StoreSettingsRequest(keyFrameId: String, startAt: Instant)
  case class StoreCaptureRequest(intervalSeconds: Int, count: Int, startAt: Instant)

  object SettingResponse {
    def apply(t: ToSetSettingDomain): SettingResponse =
      new SettingResponse(t.timestamp.time, t.id, t.test, t.shutterSpeed, t.iso, t.aperture, t.ev)
  }
  object CapturedTaskResponse {
    def apply(c: CapturedDomain): CapturedTaskResponse =
      new CapturedTaskResponse(c.timestamp.time, c.id, c.test, c.shutterSpeed, c.iso, c.aperture, c.ev, c.error, c.suggestion)
  }
}
