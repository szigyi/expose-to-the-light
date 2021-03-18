package hu.szigyi.ettl.v1.api

import java.time.Instant

import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import cats.effect.IO
import hu.szigyi.ettl.v1.api.HealthApi.HealthModel
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

class HealthApi(env: String) {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => Ok(HealthModel(Instant.now(), env, running = false))
  }
}

object HealthApi {
  case class HealthModel(time: Instant, env: String, running: Boolean)
}
