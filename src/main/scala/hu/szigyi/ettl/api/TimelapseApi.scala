package hu.szigyi.ettl.api

import cats.effect.IO
import hu.szigyi.ettl.service.TimelapseService
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class TimelapseApi(tlService: TimelapseService) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "test" =>
      Ok(tlService.test)
  }
}
