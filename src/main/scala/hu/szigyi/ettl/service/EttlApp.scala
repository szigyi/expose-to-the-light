package hu.szigyi.ettl.service

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.app.CliEttlApp.AppConfiguration
import hu.szigyi.ettl.hal.{GCamera, GConfiguration, GFile}
import hu.szigyi.ettl.model.Model.SettingsCameraModel
import hu.szigyi.ettl.service.CameraHandler.{connectToCamera, takePhoto}
import hu.szigyi.ettl.util.Timing.time

import java.nio.file.Path
import scala.concurrent.duration.Duration
import scala.util.Try

class EttlApp(appConfig: AppConfiguration, camera: GCamera, scheduler: Scheduler) extends StrictLogging {

  def execute(setting: Option[SettingsCameraModel], numberOfCaptures: Int, interval: Duration): Try[Seq[Path]] =
    for {
      config     <- connectToCamera(camera)
      imagePaths <- scheduler.schedule(numberOfCaptures, interval, capture(camera, config, numberOfCaptures, setting))
      _          <- config.close
    } yield imagePaths

  private def capture(camera: GCamera, config: GConfiguration, numberOfCaptures: Int, c: Option[SettingsCameraModel])(imageCount: Int): Try[Path] =
    for {
      _                 <- Try(c.map(adjustSettings(config, _, imageCount, numberOfCaptures)))
      imgFileOnCamera   <- capturePhoto(camera, imageCount, numberOfCaptures)
      imgPathOnComputer <- imgFileOnCamera.saveImageTo(appConfig.imageBasePath)
      _                 <- imgFileOnCamera.close
    } yield {
      logger.info(s"[$imageCount/$numberOfCaptures] Saved image: ${imgPathOnComputer.toString}")
      imgPathOnComputer
    }

  private def capturePhoto(camera: GCamera, imageCount: Int, numberOfCaptures: Int): Try[GFile] = {
    logger.info(s"[$imageCount/$numberOfCaptures] Taking photo...")
    time(s"[$imageCount/$numberOfCaptures] Capture took", takePhoto(camera))
  }

  private def adjustSettings(config: GConfiguration, c: SettingsCameraModel, imageCount: Int, numberOfCaptures: Int): Try[Unit] = {
    import cats.implicits._

    def setSS(shutterSpeedString: String): Try[Unit] =
      config.setValue("/capturesettings/shutterspeed", shutterSpeedString)

    def setI(iso: Int): Try[Unit] =
      config.setValue("/imgsettings/iso", iso.toString)

    def setA(aperture: Double): Try[Unit] =
      config.setValue("/capturesettings/aperture", aperture.toString)

    logger.info(s"[$imageCount/$numberOfCaptures] Adjusting settings: [ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]")
    val ss = c.shutterSpeedString.map(setSS)
    val i = c.iso.map(setI)
    val a = c.aperture.map(setA)
    val tryChanges: Try[List[Unit]] = List(ss, i, a).flatten.sequence
    tryChanges.flatMap(changes => {
      if (changes.nonEmpty) config.apply
      else Try(())
    })
  }
}