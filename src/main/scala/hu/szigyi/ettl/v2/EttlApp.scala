package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}
import hu.szigyi.ettl.v2.CliApp.AppConfiguration
import hu.szigyi.ettl.v2.tool.Timing.time

import java.nio.file.{Path, Paths}
import java.time.{Clock, Instant}
import scala.concurrent.duration.Duration
import scala.util.Try

class EttlApp(appConfig: AppConfiguration, camera: GCamera, scheduler: Scheduler) extends StrictLogging {

  def execute(setting: Option[SettingsCameraModel], numberOfCaptures: Int, interval: Duration, clock: Clock): Try[Seq[Path]] =
    for {
      config     <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      imagePaths <- scheduledCaptures(config, setting, numberOfCaptures, interval, clock)
      _          <- config.close
    } yield imagePaths

  private def scheduledCaptures(config: GConfiguration, setting: Option[SettingsCameraModel], numberOfCaptures: Int, interval: Duration, clock: Clock): Try[Seq[Path]] = {
    import cats.implicits._
    (0 until numberOfCaptures).toList.traverse { _ =>
      def capt: Try[Path] = capture(camera, config, setting)
      time("Schedule took", scheduler.schedule(Instant.now(clock), interval, capt))
    }
  }

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