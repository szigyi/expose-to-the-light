package hu.szigyi.ettl.v2.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toFunctorOps
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.v2.hal.{DummyCamera, GCameraImpl}
import hu.szigyi.ettl.v2.service.{EttlApp, SchedulerImpl}
import org.rogach.scallop.ScallopConf

import java.nio.file.{Path, Paths}
import java.time.Clock
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object CliApp extends IOApp with StrictLogging {
  override def run(args: List[String]): IO[ExitCode] = {
    val conf = new Conf(args)
    runApp(
      conf.dummyCamera.apply(),
      conf.imagesBasePath.apply(),
      conf.setSettings.apply(),
      conf.numberOfCaptures.apply(),
      conf.intervalSeconds.apply()
    ).as(ExitCode.Success)
  }

  private def runApp(dummyCamera: Boolean,
                     basePath: String,
                     setSettings: Boolean,
                     numberOfCaptures: Int,
                     intervalSeconds: Double): IO[Try[Seq[Path]]] = {
    val clock = Clock.systemDefaultZone()
    val appConfig = AppConfiguration(Paths.get(basePath))
    val schedulerAwakingFrequency = 100.milliseconds
    val setting = if (setSettings) Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))) else None
    val interval = Duration(intervalSeconds, TimeUnit.SECONDS)

    val ettl =
      if (dummyCamera) new EttlApp(appConfig, new DummyCamera, new SchedulerImpl(clock, schedulerAwakingFrequency))
      else new EttlApp(appConfig, new GCameraImpl, new SchedulerImpl(clock, schedulerAwakingFrequency))

    logger.info(s"           Clock: $clock")
    logger.info(s"    Dummy Camera: $dummyCamera")
    logger.info(s"Images Base Path: $basePath")
    logger.info(s"   # of Captures: $numberOfCaptures")
    logger.info(s"    Set Settings: $setSettings")
    logger.info(s"        Interval: $interval")

    IO.fromTry(ettl.execute(setting, numberOfCaptures, interval)).attempt.map {
      case Right(imagePaths) =>
        logger.info(s"App finished:")
        imagePaths.foreach(p => logger.info(p.toString))
        Success(imagePaths)
      case Left(exception) =>
        logger.error(s"App failed", exception)
        Failure(exception)
    }
  }

  class Conf(args: Seq[String]) extends ScallopConf(args) {
    val dummyCamera = opt[Boolean](name = "dummyCamera", required = false, descr = "Use dummy camera so can simulate capturing photos")
    val imagesBasePath = opt[String](name = "imagesBasePath", required = true, descr = "Folder where the captured images will be stored")
    val setSettings = opt[Boolean](name = "setSettings", descr = "Do you want the app to override the camera settings before capturing an image?")
    val numberOfCaptures = opt[Int](name = "numberOfCaptures", required = true, descr = "How many photos do you want to take?")
    val intervalSeconds = opt[Double](name = "intervalSeconds", required = true, descr = "Seconds between two captures", validate = _ > 0.1)
    verify()
  }

  case class AppConfiguration(imageBasePath: Path)

}
