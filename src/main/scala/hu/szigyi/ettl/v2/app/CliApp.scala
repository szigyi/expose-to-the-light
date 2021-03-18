package hu.szigyi.ettl.v2.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toFunctorOps
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v1.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.v2.hal.{DummyCamera, GCameraImpl}
import hu.szigyi.ettl.v2.service.{EttlApp, SchedulerImpl}
import org.rogach.scallop.ScallopConf

import java.nio.file.{Path, Paths}
import java.time.Clock
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


// 1: Can capture image and download it so can view it from computer
// 2: Can capture consecutive images without camera failure or black screen or camera lag
// 3: Can successfully change settings of camera before capturing the image
// 4: Can download lower resolution of image from camera
// 5: base path (location of saved images) is coming from app param
// 6: measure exact timing between captures and decide does it need to be more precise
// 7: change the timing from sleep to precise elapsed time as interval
// 8: can view the downloaded images via webapp           -- not part of this project
// 9: add auto startup, systemd config with log location  -- not part of this project
// 10: webapp can start and stop systemd process          -- not part of this project
// 11: webapp can view the log                            -- not part of this project
// 12: camera settings for capture is optional - just trigger capture without overwrite settings in camera
// 13: can set number of images to be taken from command line
// 14: capture image when schedule starts, do not wait until the first schedule finishes to trigger capture
// 15: use named arguments to get the command line args
// 16: interval is coming from command line as well
// 17: use better logging to inform user -> useful for task 11
// 18: DummyCamera create fake images - numbered images - https://www.baeldung.com/java-add-text-to-image
// 19: do not rename image file names, leave as original
// 20: remove unnecessary info from logs
// TODO 21: store files of one session in a dedicated timestamped directory
// TODO 22: make script that installs the app and make it runnable from commandline to macOS and debian
// TODO 23: can provide camera's settings for every capture (evaluated at the time of capture if it is possible)

object CliApp extends IOApp with StrictLogging {
  override def run(args: List[String]): IO[ExitCode] = {
    val conf = new Conf(args)
    runApp(
      conf.dummyCamera.apply(),
      conf.imagesBasePath.apply(),
      conf.setSettings.apply(),
      conf.numberOfCaptures.apply(),
      conf.intervalSeconds.apply(),
      conf.rawFileExtension.apply()
    ).as(ExitCode.Success)
  }

  private def runApp(dummyCamera: Boolean,
                     basePath: String,
                     setSettings: Boolean,
                     numberOfCaptures: Int,
                     intervalSeconds: Double,
                     rawFileExtension: String): IO[Try[Seq[Path]]] = {
    val clock = Clock.systemDefaultZone()
    val appConfig = AppConfiguration(Paths.get(basePath), if (dummyCamera) "JPG" else rawFileExtension)
    val schedulerAwakingFrequency = 100.milliseconds
    val setting = if (setSettings) Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))) else None
    val interval = Duration(intervalSeconds, TimeUnit.SECONDS)

    val ettl =
      if (dummyCamera) new EttlApp(appConfig, new DummyCamera, new SchedulerImpl(clock, schedulerAwakingFrequency))
      else new EttlApp(appConfig, new GCameraImpl, new SchedulerImpl(clock, schedulerAwakingFrequency))

    logger.info(s"             Clock: $clock")
    logger.info(s"      Dummy Camera: $dummyCamera")
    logger.info(s"  Images Base Path: $basePath")
    logger.info(s"     # of Captures: $numberOfCaptures")
    logger.info(s"      Set Settings: $setSettings")
    logger.info(s"          Interval: $interval")
    logger.info(s"Raw File Extension: $interval")

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
    val rawFileExtension = opt[String](name = "rawFileExtension", required = true, descr = "Extension of your Raw file ie: CR2, NEF")
    verify()
  }

  case class AppConfiguration(imageBasePath: Path, rawFileExtension: String)

}
