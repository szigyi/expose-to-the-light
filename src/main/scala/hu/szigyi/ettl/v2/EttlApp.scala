package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}

import java.nio.file.Path
import scala.util.Try

object EttlApp extends StrictLogging {

  def runEttl(camera: GCamera, imageBasePath: Path): Try[Unit] = {
    for {
      config      <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      cameraFiles <- scheduledCaptures(camera, imageBasePath)
    } yield {
      cameraFiles.foreach(_.close)
      config.close
    }
  }

  private def scheduledCaptures(camera: GCamera, imageBasePath: Path): Try[Seq[GFile]] = {
    for {
      first  <- takePhoto(camera)
      _      <- first.saveImageTo(imageBasePath.resolve("IMG_1.CR2"))
      second <- takePhoto(camera)
      _      <- second.saveImageTo(imageBasePath.resolve("IMG_2.CR2"))
    } yield Seq(first, second)
  }
}
