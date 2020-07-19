package hu.szigyi.ettl.api

import java.time.Instant

import cats.effect.IO
import hu.szigyi.ettl.api.ApiTool.InstantVar
import hu.szigyi.ettl.client.influx.InfluxDomain.{Captured, TimelapseTask}
import hu.szigyi.ettl.service.CameraService.CameraError
import hu.szigyi.ettl.service.TimelapseService
import io.circe.Encoder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class TimelapseApi(tlService: TimelapseService) {
  import TimelapseApi._

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "captured" / InstantVar(from) =>
      Ok(tlService.getCapturedTasks(from).map(_.map(CapturedTaskResponse.apply)))
    case GET -> Root / "test" =>
      Ok(tlService.storeTestTimelapseTask.map(_.map(TimelapseTaskResponse.apply)))
  }
}

object TimelapseApi {
  implicit val cameraErrorEncoder: Encoder[CameraError] =
    Encoder.forProduct3("msg", "result", "suggestion")(c => (c.msg, c.result, c.suggestion))
  implicit val executedResponseEncoder: Encoder[CapturedTaskResponse] =
    Encoder.forProduct9("timestamp", "id", "test", "shutterSpeed", "iso",
      "aperture", "ev", "error", "suggestion")(t => (t.timestamp,
      t.id, t.test, t.shutterSpeed, t.iso, t.aperture, t.ev, t.error, t.suggestion))
  implicit val timelapseTaskResponseEncoder: Encoder[TimelapseTaskResponse] =
    Encoder.forProduct7("timestamp", "id", "test", "shutterSpeed", "iso",
      "aperture", "ev")(t => (t.timestamp, t.id, t.test, t.shutterSpeed, t.iso, t.aperture, t.ev))

  case class TimelapseTaskResponse(timestamp: Instant,
                                   id: String,
                                   test: Boolean,
                                   shutterSpeed: Double,
                                   iso: Int,
                                   aperture: Double,
                                   ev: Double)
  object TimelapseTaskResponse {
    def apply(t: TimelapseTask): TimelapseTaskResponse =
      new TimelapseTaskResponse(t.timestamp.time, t.id, t.test, t.shutterSpeed, t.iso, t.aperture, t.ev)
  }
  case class CapturedTaskResponse(timestamp: Instant,
                                  id: String,
                                  test: Boolean,
                                  shutterSpeed: Double,
                                  iso: Int,
                                  aperture: Double,
                                  ev: Double,
                                  error: Option[String],
                                  suggestion: Option[String])
  object CapturedTaskResponse {
    def apply(c: Captured): CapturedTaskResponse =
      new CapturedTaskResponse(c.timestamp.time, c.id, c.test, c.shutterSpeed, c.iso, c.aperture, c.ev, c.error, c.suggestion)
  }
}
