package hu.szigyi.ettl.v1

import cats.data.Kleisli
import cats.effect.{ContextShift, ExitCode, IO, Timer}
import hu.szigyi.ettl.v1.api._
import hu.szigyi.ettl.v1.util.ManifestReader
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}

class HttpApi(env: String, port: Int, staticApi: StaticApi, healthApi: HealthApi, keyFrameApi: KeyFrameApi,
              timelapseApi: TimelapseApi, lastCapturedApi: LastCapturedApi)(implicit timer: Timer[IO], contextShift: ContextShift[IO]) {

  def run: fs2.Stream[IO, ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .withBanner(Seq(banner(env)))
      .withHttpApp(httpApp)
      .serve

  private def httpApp: Kleisli[IO, Request[IO], Response[IO]] =
    Router(
      "/"               -> staticApi.service,
      "/health"         -> healthApi.service,
      "/key-frames"     -> keyFrameApi.service,
      "/timelapse"      -> timelapseApi.service,
      "/last-captured"  -> lastCapturedApi.service
    ).orNotFound

  private def banner(envName: String): String = {
    val manifestInfo = ManifestReader(getClass).manifestInfo()
    s"""
       |          _       _       _
       |  ___ ___| |_ ___| |_ ___| |
       | / -_)___|  _|___|  _|___| |
       | \\___|    \\__|    \\__|   |_|
       |
       | Build Number: ${manifestInfo.buildNumber}
       | Build Time:   ${manifestInfo.buildTimeStamp}
       | Git Hash:     ${manifestInfo.gitHash}
       | ENV:          ${envName.toUpperCase}""".stripMargin
  }
}
