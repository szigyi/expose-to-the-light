package hu.szigyi.ettl.api

import cats.effect.{Blocker, ContextShift, IO}
import org.http4s.server.staticcontent._

class StaticApi(blocker: Blocker)(implicit cs: ContextShift[IO]) {

  val service = resourceService[IO](ResourceService.Config("/webApp", blocker))
}
