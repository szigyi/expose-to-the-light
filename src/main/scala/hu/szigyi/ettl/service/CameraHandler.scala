package hu.szigyi.ettl.service

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.hal.{GCamera, GConfiguration, GFile}
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
      case e: GPhotoException if e.result == -105 =>
        logger.error("gphoto2 does not recognise your device. *Is it turned on? *Is the USB cable connected? *Is it in PTP mode? Turn off Wifi in the camera for example.")
        Failure(e)
      case unrecoverableException =>
        Failure(unrecoverableException)
    }

  private def initialiseCamera(camera: GCamera): Try[GConfiguration] = {
    logger.info("Connecting to camera...")
    camera.initialize.flatMap { _ =>
      camera.newConfiguration.map { configuration =>
        logger.trace("Getting settings names:")
        logger.trace(configuration.getNames.toString)
        val imageFormat = "RAW + Tiny JPEG" // This is important as we want to download the JPG version to use it later
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
