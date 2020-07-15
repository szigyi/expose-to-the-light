package hu.szigyi.ettl.api

import cats.effect.IO
import hu.szigyi.ettl.api.TimelapseApi.TimelapseResponse
import hu.szigyi.ettl.service.CameraService.CameraError
import hu.szigyi.ettl.service.TimelapseService
import io.circe.Encoder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class TimelapseApi(tlService: TimelapseService) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "test" =>
      Ok(tlService.test.map {
        case Right(value) => TimelapseResponse(None, Some(value))
        case Left(value) => TimelapseResponse(Some(value), None)
      })
  }
}

object TimelapseApi {
  implicit val cameraErrorEncoder: Encoder[CameraError] =
    Encoder.forProduct3("msg", "result", "suggestion")(c => (c.msg, c.result, c.suggestion))
  implicit val timelapseResponseEncoder: Encoder[TimelapseResponse] =
    Encoder.forProduct2("error", "result")(t => (t.error, t.result))

  case class TimelapseResponse(error: Option[CameraError], result: Option[String])
}
