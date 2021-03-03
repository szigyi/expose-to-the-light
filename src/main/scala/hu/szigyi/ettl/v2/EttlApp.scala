package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}
import hu.szigyi.ettl.v2.CliApp.AppConfiguration
import hu.szigyi.ettl.v2.tool.Timing.time

import java.nio.file.{Path, Paths}
import scala.concurrent.duration.Duration
import scala.util.Try

class EttlApp(appConfig: AppConfiguration, camera: GCamera, scheduler: Scheduler) extends StrictLogging {

  def execute(setting: Option[SettingsCameraModel], numberOfCaptures: Int, interval: Duration): Try[Seq[Path]] =
    for {
      config     <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      imagePaths <- scheduler.schedule(numberOfCaptures, interval, capture(camera, config, _, setting))
      _          <- config.close
    } yield imagePaths

  private def capture(camera: GCamera, config: GConfiguration, imageCount: Int, c: Option[SettingsCameraModel]): Try[Path] =
    for {
      _                 <- Try(c.map(adjustSettings(config, _, imageCount)))
      imgFileOnCamera   <- capturePhoto(camera, imageCount)
      imgPathOnComputer <- imgFileOnCamera.saveImageTo(appConfig.imageBasePath.resolve(imageNameGenerator))
      _                 <- imgFileOnCamera.close
    } yield {
      logger.info(s"[$imageCount] Saved image: ${imgPathOnComputer.toString}")
      imgPathOnComputer
    }

  private def capturePhoto(camera: GCamera, imageCount: Int): Try[GFile] = {
    logger.info(s"[$imageCount] Taking photo...")
    time(s"[$imageCount] Capture took", takePhoto(camera))
  }

  private def adjustSettings(config: GConfiguration, c: SettingsCameraModel, imageCount: Int): Try[Unit] = {
    import cats.implicits._

    def setSS(shutterSpeedString: String): Try[Unit] =
      config.setValue("/capturesettings/shutterspeed", shutterSpeedString)

    def setI(iso: Int): Try[Unit] =
      config.setValue("/imgsettings/iso", iso.toString)

    def setA(aperture: Double): Try[Unit] =
      config.setValue("/capturesettings/aperture", aperture.toString)

    logger.info(s"[$imageCount] Adjusting settings: [ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]")
    val ss = c.shutterSpeedString.map(setSS)
    val i = c.iso.map(setI)
    val a = c.aperture.map(setA)
    val tryChanges: Try[List[Unit]] = List(ss, i, a).flatten.sequence
    tryChanges.flatMap(changes => {
      if (changes.nonEmpty) config.apply
      else Try()
    })
  }

  private var counter = 0
  private def imageNameGenerator: Path = {
    counter = counter + 1
    Paths.get(s"IMG_$counter.CR2")
  }
}