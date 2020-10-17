package hu.szigyi.ettl.api

import cats.effect.IO
import hu.szigyi.ettl.service.CapturedImageService
import org.http4s.dsl.io._
import org.http4s.{Header, Headers, HttpRoutes, Response}

class LastCapturedApi(capturedImageService: CapturedImageService) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      val headers = Headers.of(Header("Content-Type", "image/png"))
      val body = fs2.Stream.evalSeq(capturedImageService.image.get.map(_.getBytes.toSeq))
      IO(Response[IO](headers = headers, body = body))
  }
}
