package hu.szigyi.ettl.service

import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.{CameraError, GenericCameraError, OfflineCamera}
import hu.szigyi.ettl.util.ShellKill
import hu.szigyi.ettl.util.ShutterSpeedMap._
import org.gphoto2.{Camera, CameraUtils, CameraWidgets, GPhotoException}

import scala.util.{Failure, Success, Try}

class CameraService(kill: ShellKill) extends StrictLogging {
  private val camera = new Camera()

  private def initialise: CameraWidgets = {
    camera.initialize()
    camera.newConfiguration()
  }

  private def acquireRootWidget: Try[CameraWidgets] =
    Try(camera.newConfiguration())

  def useCamera(doYourThing: (=> Try[CameraWidgets]) => Try[Unit]): Either[CameraError, String] = {
    val result = Try(initialise).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        kill.killGPhoto2Processes
        Try(camera.newConfiguration())
      case unrecoverableException =>
        Failure(unrecoverableException)

    }.flatMap(_ => doYourThing(acquireRootWidget)) match {
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

  def setSettings(acquireRootWidget: => Try[CameraWidgets], shutterSpeed: Double, iso: Int, aperture: Double): Try[Unit] = {
    toShutterSpeed(shutterSpeed).map(ss => {
      logger.info(s"[ss: $ss, i: ${iso.toString}, a: $aperture]")
      setConfig(acquireRootWidget, "/capturesettings/shutterspeed", ss)
    })
    setConfig(acquireRootWidget, "/imgsettings/iso", iso.toString)
    setConfig(acquireRootWidget, "/capturesettings/aperture", aperture.toString)
  }

  private def setConfig(acquireRootWidget: => Try[CameraWidgets], key: String, value: String): Try[Unit] =
    acquireRootWidget.map(rootWidget => {
      rootWidget.setValue(key, value)
      rootWidget.apply()
      CameraUtils.closeQuietly(rootWidget)
    })
}

object CameraService {

  trait CameraError {
    val msg: String
    val result: Int
    val suggestion: Option[String]
  }

  case class OfflineCamera(msg: String) extends CameraError {
    override val result: Int = -105
    override val suggestion: Option[String] = Some(
      """
        |Camera is not accessible. App cannot connect to it.
        |Please check the followings:
        |- Is the camera turned on?
        |- Is the camera connected to this computer via USB cable?
        |- Is the camera in PTP mode? (Disable wifi module for Canon!)
        |""".stripMargin)
  }
  case class GenericCameraError(msg: String, result: Int, suggestion: Option[String]) extends CameraError
}