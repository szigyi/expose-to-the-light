package hu.szigyi.ettl.api

import java.time.Instant

import scala.util.Try

object ApiTool {
  object InstantVar {
    def unapply(arg: String): Option[Instant] = Try(Instant.parse(arg)).toOption
  }
}
