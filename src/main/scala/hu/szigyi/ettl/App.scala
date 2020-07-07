package hu.szigyi.ettl

import cats.syntax.functor._
import cats.data.Kleisli
import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.util.ManifestReader
import org.http4s.server.Router
import org.http4s.{Request, Response}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

object App extends IOApp with StrictLogging {

  private val port = sys.env.getOrElse("http_port", "8230").toInt
  private val env = sys.env.getOrElse("ENV", "local")

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .withBanner(Seq(banner(env)))
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .handleErrorWith(logErrorAsAFinalFrontier)
      .map(_ => finalWords())
      .as(ExitCode.Success)
  }

  private def httpApp: Kleisli[IO, Request[IO], Response[IO]] = {
    val ioc = new InverseOfControl(env)

    Router(
      "/"           -> ioc.staticApi.service,
      "/health"     -> ioc.healthApi.service,
      "/settings"   -> ioc.settingsApi.service
    ).orNotFound
  }

  private def logErrorAsAFinalFrontier(throwable: Throwable): IO[Unit] = {
    logger.error(s"App is terminating because of ${throwable.getMessage}")
    IO.raiseError(throwable)
  }

  private def finalWords(): Unit =
    logger.info(s"App is terminating as you said so!")


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
