package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}

import scala.util.Try

object EttlApp extends StrictLogging {

  def runEttl(camera: GCamera): Try[Unit] = {
    for {
      config      <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      cameraFiles <- scheduledCaptures(camera)
    } yield {
      cameraFiles.foreach(_.close)
      config.close
      logger.info("Terminating...")
    }
  }

  private def scheduledCaptures(camera: GCamera): Try[Seq[GFile]] = {
    for {
      first <- takePhoto(camera)
      second <- takePhoto(camera)
    } yield Seq(first, second)
  }
}
