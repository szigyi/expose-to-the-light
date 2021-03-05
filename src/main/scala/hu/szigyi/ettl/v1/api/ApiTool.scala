package hu.szigyi.ettl.v1.api

import java.time.{Instant, ZonedDateTime}

import scala.util.Try

object ApiTool {
  object InstantVar {
    def unapply(arg: String): Option[Instant] = Try(Instant.parse(arg)).toOption
  }

  object ZonedDateTimeVar {
    def unapply(arg: String): Option[ZonedDateTime] = Try(ZonedDateTime.parse(arg)).toOption
  }
}
