package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}
import hu.szigyi.ettl.v2.CliApp.AppConfiguration

import java.nio.file.{Path, Paths}
import scala.util.Try

class EttlApp(appConfig: AppConfiguration) extends StrictLogging {

  def execute(camera: GCamera): Try[Seq[Path]] =
    for {
      config     <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      imagePaths <- scheduledCaptures(camera, config)
      _          <- config.close
    } yield imagePaths

  private def scheduledCaptures(camera: GCamera, config: GConfiguration): Try[Seq[Path]] =
    for {
      first  <- capture(camera, config, Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))))
      second <- capture(camera, config, Some(SettingsCameraModel(Some(1d), Some(100), Some(2.8))))
    } yield Seq(first, second)

  private def capture(camera: GCamera, config: GConfiguration, c: Option[SettingsCameraModel]): Try[Path] =
    for {
      _                 <- Try(c.map(adjustSettings(config, _)))
      imgFileOnCamera   <- takePhoto(camera)
      imgPathOnComputer <- imgFileOnCamera.saveImageTo(appConfig.imageBasePath.resolve(imageNameGenerator))
      _                 <- imgFileOnCamera.close
    } yield imgPathOnComputer

  private def adjustSettings(config: GConfiguration, c: SettingsCameraModel): Try[Unit] = {
    import cats.implicits._

    def setSS(shutterSpeedString: String): Try[Unit] =
      config.setValue("/capturesettings/shutterspeed", shutterSpeedString)

    def setI(iso: Int): Try[Unit] =
      config.setValue("/imgsettings/iso", iso.toString)

    def setA(aperture: Double): Try[Unit] =
      config.setValue("/capturesettings/aperture", aperture.toString)

    logger.info(s"[ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]")
    val ss = c.shutterSpeedString.map(setSS(_))
    val i = c.iso.map(setI(_))
    val a = c.aperture.map(setA(_))
    val tryChanges: Try[List[Unit]] = List(ss, i, a).flatten.sequence
    tryChanges.flatMap(changes => {
      if (changes.nonEmpty) config.apply
      else Try()
    })
  }

  var counter = 0
  private def imageNameGenerator: Path = {
    counter = counter + 1
    Paths.get(s"IMG_$counter.CR2")
  }
}