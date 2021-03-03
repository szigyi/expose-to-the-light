package hu.szigyi.ettl.v2

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.SettingsCameraModel

import java.nio.file.{Path, Paths}
import java.time.Clock
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

// 1: Can capture image and download it so can view it from computer
// 2: Can capture consecutive images without camera failure or black screen or camera lag
// 3: Can successfully change settings of camera before capturing the image
// TODO 4: Can download lower resolution of image from camera
// 5: base path (location of saved images) is coming from app param
// 6: measure exact timing between captures and decide does it need to be more precise
// TODO 7: change the timing from sleep to precise elapsed time as interval
// TODO 8: can view the downloaded images via webapp
// TODO 9: add auto startup, systemd config with log location
// TODO 10: webapp can start and stop systemd process
// TODO 11: webapp can view the log
// 12: camera settings for capture is optional - just trigger capture without overwrite settings in camera

object CliApp extends IOApp with StrictLogging {
  override def run(args: List[String]): IO[ExitCode] = {
    if (args.size != 2) {
      logger.error("Base Path as first program argument is missing")
      logger.error("Set Settings as second program argument is missing")
      IO.pure(ExitCode.Error)
    } else {
      val basePath = args(0)
      val setSettings = args(1).toBoolean
      runApp(basePath, setSettings)
        .as(ExitCode.Success)
    }
  }

  private def runApp(basePath: String, setSettings: Boolean): IO[Try[Seq[Path]]] = {
    val clock = Clock.systemDefaultZone()
    val appConfig = AppConfiguration(Paths.get(basePath))
    val ettl = new EttlApp(appConfig, new GCameraImpl, new SchedulerImpl(clock))
    val setting = if (setSettings) Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))) else None
    val numberOfCaptures = 100
    val interval = 2.seconds // TODO: validate interval should not be less than 1 milliseconds

    IO.fromTry(ettl.execute(setting, numberOfCaptures, interval, clock)).attempt.map {
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