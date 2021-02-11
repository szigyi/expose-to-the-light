package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import org.gphoto2.{CameraFile, GPhotoException}

import scala.util.{Failure, Try}

object CameraHandler extends StrictLogging {

  def takePhoto(camera: GCamera): Try[CameraFile] =
    Try(camera.captureImage())

  def connectToCamera(camera: GCamera, shellKill: => Unit): Try[GCameraConfiguration] =
    Try(initialiseCamera(camera)).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        shellKill
        Try(initialiseCamera(camera))
      case unrecoverableException =>
        Failure(unrecoverableException)
    }

  private def initialiseCamera(camera: GCamera): GCameraConfiguration = {
    camera.initialize()
    val configuration = camera.newConfiguration()
    logger.trace("Getting settings names:")
    logger.trace(configuration.getNames.toString)
    val imageFormat = "RAW"
    configuration.setValue("/settings/capturetarget", "Memory card")
    configuration.setValue("/imgsettings/imageformat", imageFormat)
    configuration.setValue("/imgsettings/imageformatsd", imageFormat)
    configuration.setValue("/capturesettings/drivemode", "Single silent")
    configuration.apply()
    configuration
  }
}
