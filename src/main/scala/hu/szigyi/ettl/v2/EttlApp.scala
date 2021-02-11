package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}

import java.nio.file.Path
import scala.util.Try

object EttlApp extends StrictLogging {

  def runEttl(camera: GCamera, imageBasePath: Path): Try[Seq[Path]] =
    for {
      config     <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      imagePaths <- scheduledCaptures(camera, imageBasePath)
      _          <- config.close
    } yield imagePaths

  private def scheduledCaptures(camera: GCamera, imageBasePath: Path): Try[Seq[Path]] = {
    for {
      first  <- capture(camera, imageBasePath.resolve("IMG_1.CR2"))
      second <- capture(camera, imageBasePath.resolve("IMG_2.CR2"))
    } yield Seq(first, second)
  }

  private def capture(camera: GCamera, imagePath: Path): Try[Path] =
    for {
      imgFileOnCamera   <- takePhoto(camera)
      imgPathOnComputer <- imgFileOnCamera.saveImageTo(imagePath)
      _                 <- imgFileOnCamera.close
    } yield imgPathOnComputer

  private def imageNameGenerator: String = ???
}