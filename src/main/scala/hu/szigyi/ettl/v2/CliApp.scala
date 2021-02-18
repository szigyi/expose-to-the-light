package hu.szigyi.ettl.v2

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging

import java.nio.file.{Path, Paths}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

// 1: Can capture image and download it so can view it from computer
// 2: Can capture consecutive images without camera failure or black screen or camera lag
// 3: Can successfully change settings of camera before capturing the image
// TODO 4: Can download lower resolution of image from camera
// TODO 5: base path (location of saved images) is coming from app param

object CliApp extends IOApp with StrictLogging {
  override def run(args: List[String]): IO[ExitCode] = {
    if (args.size != 1) {
      logger.error("Base Path as first program argument is missing")
      IO.pure(ExitCode.Error)
    } else {
      val basePath = args(0)
      runApp(basePath)
        .as(ExitCode.Success)
    }
  }

  private def runApp(basePath: String): IO[Try[Seq[Path]]] = {
    val appConfig = AppConfiguration(Paths.get(basePath))
    val ettl = new EttlApp(appConfig, new GCameraImpl)
    val numberOfCaptures = 3
    val interval = 3.seconds

    IO.fromTry(ettl.execute(numberOfCaptures, interval)).attempt.map {
      case Right(imagePaths) =>
        logger.info(s"App finished: \n${imagePaths.mkString("\n")}")
        Success(imagePaths)
      case Left(exception) =>
        logger.error(s"App failed", exception)
        Failure(exception)
    }
  }

  case class AppConfiguration(imageBasePath: Path)

}