package hu.szigyi.ettl.service

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.{CameraError, CaptureSetting, GenericCameraError, OfflineCamera}
import hu.szigyi.ettl.util.{ShellKill, ShutterSpeedMap}
import org.gphoto2.{Camera, CameraUtils, CameraWidgets, GPhotoException}

import scala.util.{Failure, Try}

class CameraService(kill: ShellKill) extends StrictLogging {
  private val camera = new Camera()

  private def initialise: Unit = {
    camera.initialize()
    val testConf = camera.newConfiguration()
    CameraUtils.closeQuietly(testConf)
  }

  def useCamera(restOfTheApp: CameraWidgets => IO[Unit]): IO[Either[CameraError, String]] = {
    IO.fromTry(Try(initialise).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        kill.killGPhoto2Processes
        Try(camera.newConfiguration())
      case unrecoverableException =>
        Failure(unrecoverableException)

    }).flatMap(_ => restOfTheApp(camera.newConfiguration())).attempt.map {
      case Right(_) =>
        logger.debug("Task is done")
        Right("Task is done")
      case Left(e: GPhotoException) if e.result == -105 =>
        val error = OfflineCamera(e.getMessage)
        logger.error(error.msg)
        Left(error)
      case Left(e: GPhotoException) =>
        logger.error(e.getMessage, e)
        Left(GenericCameraError(e.getMessage, e.result, None))
      case Left(e) =>
        logger.error(e.getMessage, e)
        Left(GenericCameraError(e.getMessage, 0, None))
    }.map(res => {
      CameraUtils.closeQuietly(camera)
      res
    })
  }

  def setEvSettings(rootWidget: CameraWidgets, c: CaptureSetting): IO[Unit] =
    IO.fromTry(Try {
      logger.info(s"[ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]")
      rootWidget.setValue("/capturesettings/shutterspeed", c.shutterSpeedString)
      rootWidget.setValue("/imgsettings/iso", c.iso.toString)
      rootWidget.setValue("/capturesettings/aperture", c.aperture.toString)
      rootWidget.apply()
    })

  def captureImage: IO[Unit] = {
    IO.fromTry(Try {
      logger.info("Capturing image...")
      val cameraFile = camera.captureImage()
      CameraUtils.closeQuietly(cameraFile)
    })
  }
}

object CameraService {

  case class CaptureSetting(shutterSpeed: Double, shutterSpeedString: String, iso: Int, aperture: Double)
  object CaptureSetting {
    def apply(shutterSpeed: Double, iso: Int, aperture: Double): Option[CaptureSetting] =
      ShutterSpeedMap.toShutterSpeed(shutterSpeed).map(sss => {
        new CaptureSetting(shutterSpeed, sss, iso, aperture)
      })
  }

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