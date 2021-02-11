package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.v2.CameraHandler.{connectToCamera, takePhoto}
import org.gphoto2.{CameraFile, CameraUtils}

import scala.util.Try

object EttlApp extends StrictLogging {

  def runEttl(camera: GCamera): Try[Unit] = {
    for {
      _ <- Try(logger.info("Connecting to the camera..."))
      config <- connectToCamera(camera, ShellKill.killGPhoto2Processes)
      _ <- Try(logger.info("Capturing images..."))
      cameraFiles <- scheduledCaptures(camera)
    } yield {
      cameraFiles.map(cameraFile => CameraUtils.closeQuietly(cameraFile))
      config.close
      logger.info("Terminating...")
    }
  }

  private def scheduledCaptures(camera: GCamera): Try[Seq[CameraFile]] = {
    for {
      first <- takePhoto(camera)
      second <- takePhoto(camera)
    } yield Seq(first, second)
  }
}
