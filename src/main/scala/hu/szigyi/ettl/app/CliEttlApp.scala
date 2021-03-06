package hu.szigyi.ettl.app

import cats.effect.{ExitCase, ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.hal.{DummyCamera, GCamera, GCameraImpl}
import hu.szigyi.ettl.model.Model.SettingsCameraModel
import hu.szigyi.ettl.service.{DirectoryService, EttlApp, SchedulerImpl}
import org.rogach.scallop.{ScallopConf, ScallopOption}

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
// 21: store files of one session in a dedicated timestamped directory
// 22: make script that installs the app and make it runnable from commandline to macOS and debian
// TODO 23: can provide camera's settings for every capture (evaluated at the time of capture if it is possible)
// 24: emergency shutdown: cancel the execution (cancel fs2 stream?) -- IOApp receives SIGABORT and SIGINT
// 25: make the install script download the ettl script as well not just the artifact
// 26: copy gphoto2-java into the project and compile with the app
// TODO 27: try to use `gphoto2 --capture-image-and-download --keep-raw` feature to not download the entire RAW and speed up the process, shrinking needed time between captures
// TODO 28: create baseline how much time needed between image capturing
// TODO 29: add description, project's goal to README
// TODO 30: lists the main features of the app in README
// TODO 31: link a video that showcases the features in README
// TODO 32: lists cameras that the app was tested/used with

object CliEttlApp extends IOApp with StrictLogging {
  override def run(args: List[String]): IO[ExitCode] = {
    val conf             = new Conf(args)
    val isDummyCamera    = conf.dummyCamera.apply()
    val camera           = if (isDummyCamera) new DummyCamera else new GCameraImpl
    val rawFileExtension = if (isDummyCamera) "JPG" else conf.rawFileExtension.apply()
    IO.suspend {
        runApp(
          isDummyCamera,
          camera,
          conf.imagesBasePath.apply(),
          conf.setSettings.apply(),
          conf.numberOfCaptures.apply(),
          conf.intervalSeconds.apply(),
          rawFileExtension
        ).as(ExitCode.Success)
      }
      .guaranteeCase {
        case ExitCase.Completed =>
          logger.info(s"App finished")
          IO.fromTry(camera.close())
        case ExitCase.Error(e) =>
          logger.error(s"App failed: ${e.getMessage}")
          IO.fromTry(camera.close())
        case ExitCase.Canceled =>
          logger.error(s"App is cancelled")
          IO.fromTry(camera.close())
      }
  }

  private def runApp(dummyCamera: Boolean,
                     camera: GCamera,
                     basePath: String,
                     setSettings: Boolean,
                     numberOfCaptures: Int,
                     intervalSeconds: Double,
                     rawFileExtension: String): IO[Seq[Path]] = {
    val clock                     = Clock.systemDefaultZone()
    val appConfig                 = AppConfiguration(Paths.get(basePath), rawFileExtension)
    val schedulerAwakingFrequency = 100.milliseconds
    val setting                   = if (setSettings) Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))) else None
    val interval                  = Duration(intervalSeconds, TimeUnit.SECONDS)

    val scheduler = new SchedulerImpl(clock, schedulerAwakingFrequency)
    val dir       = new DirectoryService
    val ettl      = new EttlApp(appConfig, camera, scheduler, dir, clock.instant())

    logger.info(s"             Clock: $clock")
    logger.info(s"      Dummy Camera: $dummyCamera")
    logger.info(s"  Images Base Path: $basePath")
    logger.info(s"     # of Captures: $numberOfCaptures")
    logger.info(s"      Set Settings: $setSettings")
    logger.info(s"          Interval: $interval")
    logger.info(s"Raw File Extension: $rawFileExtension")

    IO.fromTry(ettl.execute(setting, numberOfCaptures, interval))
  }

  class Conf(args: Seq[String]) extends ScallopConf(args) {
    val dummyCamera: ScallopOption[Boolean]     = opt[Boolean](name = "dummyCamera", required = false, descr = "Use dummy camera so can simulate capturing photos")
    val imagesBasePath: ScallopOption[String]   = opt[String](name = "imagesBasePath", required = true, descr = "Folder where the captured images will be stored")
    val setSettings: ScallopOption[Boolean]     = opt[Boolean](name = "setSettings", descr = "Do you want the app to override the camera settings before capturing an image?")
    val numberOfCaptures: ScallopOption[Int]    = opt[Int](name = "numberOfCaptures", required = true, descr = "How many photos do you want to take?")
    val intervalSeconds: ScallopOption[Double]  = opt[Double](name = "intervalSeconds", required = true, descr = "Seconds between two captures", validate = _ > 0.1)
    val rawFileExtension: ScallopOption[String] = opt[String](name = "rawFileExtension", required = true, descr = "Extension of your Raw file ie: CR2, NEF")
    verify()
  }

  case class AppConfiguration(imageBasePath: Path, rawFileExtension: String)

}
