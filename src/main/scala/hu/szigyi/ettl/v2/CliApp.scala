package hu.szigyi.ettl.v2

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v2.EttlApp.runEttl

import scala.util.{Failure, Success}

// TODO 1: Can capture image and download it so can view it from computer
// TODO 2: Can capture consecutive images without camera failure or black screen or camera lag

object CliApp extends IOApp with StrictLogging {
  override def run(args: List[String]): IO[ExitCode] =
    IO(runEttl(new GCameraImpl())).map {
      case Success(v) =>
        logger.info(s"App finished: $v")
        Success(v)
      case Failure(exception) =>
        logger.error(s"App failed: ${exception}")
        Failure(exception)
    }.as(ExitCode.Success)
}
