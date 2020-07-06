package hu.szigyi.ettl.api


import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import cats.effect.IO
import hu.szigyi.ettl.service.Curvature
import hu.szigyi.ettl.service.Curvature.SettingsWithTime
import io.circe.Encoder
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

import CurvatureApi._

class CurvatureApi {

  val service = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(Curvature.settings.map(toCurvatureModel))
  }
}

object CurvatureApi {

  case class CurvatureModel(duration: Long, shutterSpeed: Double, iso: Int)

  def toCurvatureModel(s: SettingsWithTime): CurvatureModel =
    CurvatureModel(s.duration.toNanos, s.shutterSpeed, s.iso)


//  import java.time.{Duration => JavaDuration}
//
//  implicit val durationEncoder: Encoder[JavaDuration] = Encoder.encodeDuration
//  implicit val seqSettings: Encoder[SettingsWithTime] =
//    Encoder.forProduct3("duration", "shutterSpeed", "iso")(s => (toJavaDuration(s.duration), s.shutterSpeed, s.iso))
//
//  private def toJavaDuration(d: Duration): JavaDuration =
//    JavaDuration.ofNanos(d.toNanos)
}
