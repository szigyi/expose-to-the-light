package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import org.gphoto2.GPhotoException

import scala.util.{Failure, Try}

object CameraHandler extends StrictLogging {

  def takePhoto(camera: GCamera): Try[GFile] =
    camera.captureImage

  def connectToCamera(camera: GCamera, shellKill: => Unit): Try[GConfiguration] =
    initialiseCamera(camera).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        logger.warn("Executing shell kill and retry connecting to the camera...")
        shellKill
        initialiseCamera(camera)
      case unrecoverableException =>
        Failure(unrecoverableException)
    }

  private def initialiseCamera(camera: GCamera): Try[GConfiguration] = {
    logger.info("Connecting to camera...")
    camera.initialize.flatMap { _ =>
      camera.newConfiguration.map { configuration =>
        logger.trace("Getting settings names:")
        logger.trace(configuration.getNames.toString)
        val imageFormat = "RAW + Tiny JPEG"
        configuration.setValue("/settings/capturetarget", "Memory card")
        configuration.setValue("/imgsettings/imageformat", imageFormat)
        configuration.setValue("/imgsettings/imageformatsd", imageFormat)
        configuration.setValue("/capturesettings/drivemode", "Single silent")
        configuration.apply
        configuration
      }
    }
  }
}
