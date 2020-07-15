package hu.szigyi.ettl.service

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.{CameraError, GenericCameraError, OfflineCamera}
import hu.szigyi.ettl.util.ShellKill
import org.gphoto2.{Camera, CameraUtils, CameraWidgets, GPhotoException}

import scala.util.{Failure, Success, Try}

class CameraService(kill: ShellKill) extends StrictLogging {

  def useCamera(doYourThing: CameraWidgets => Unit): Either[CameraError, String] = {
    val camera = new Camera()

    val result = Try(camera.newConfiguration()).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        kill.killGPhoto2Processes
        Try(camera.newConfiguration())
      case unrecoverableException =>
        Failure(unrecoverableException)

    }.flatMap(rootWidget => Try(doYourThing(rootWidget))) match {
      case Success(_) =>
        logger.info("Task is done")
        Right("Task is done")
      case Failure(e: GPhotoException) if e.result == -105 =>
        val error = OfflineCamera(e.getMessage)
        logger.error(error.msg)
        Left(error)
      case Failure(e: GPhotoException) =>
        logger.error(e.getMessage, e)
        Left(GenericCameraError(e.getMessage, e.result, None))
      case Failure(e) =>
        logger.error(e.getMessage, e)
        Left(GenericCameraError(e.getMessage, 0, None))

    }
    CameraUtils.closeQuietly(camera)
    result
  }

  def setSettings(rootWidget: CameraWidgets, shutterSpeed: Double, iso: Int, aperture: Double): Unit = {
    rootWidget.setValue("shutterspeed", shutterSpeed)
    rootWidget.setValue("iso", iso)
    rootWidget.setValue("aperture", aperture)
  }
}

object CameraService {
  trait CameraError {
    val msg: String
    val result: Int
    val suggestion: Option[String]
  }
  case class OfflineCamera(msg: String) extends CameraError {
    override val result: Int = -105
    override val suggestion: Option[String] = Some("""
                                                |Camera is not accessible. App cannot connect to it.
                                                |Please check the followings:
                                                |- Is the camera turned on?
                                                |- Is the camera connected to this computer via USB cable?
                                                |- Is the camera in PTP mode? (Disable wifi module for Canon!)
                                                |""".stripMargin)
  }
  case class GenericCameraError(msg: String, result: Int, suggestion: Option[String]) extends CameraError
}