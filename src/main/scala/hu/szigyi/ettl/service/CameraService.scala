package hu.szigyi.ettl.service

import java.time.ZonedDateTime

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.{CameraError, Capture, GenericCameraError, OfflineCamera}
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

  private def acquireRootWidget: IO[CameraWidgets] =
    IO.fromTry(Try(camera.newConfiguration()))

  def useCamera(restOfTheApp: => IO[List[Unit]]): IO[Either[CameraError, String]] = {
    IO.fromTry(Try(initialise).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        kill.killGPhoto2Processes
        Try(camera.newConfiguration())
      case unrecoverableException =>
        Failure(unrecoverableException)

    }).flatMap(_ => restOfTheApp).attempt.map {
      case Right(_) =>
        logger.info("Task is done")
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

  def setEvSettings(c: Capture): IO[Unit] =
    for {
      _ <- IO(logger.info(s"[ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]"))
      _ <- setConfig("/capturesettings/shutterspeed", c.shutterSpeedString)
      _ <- setConfig("/imgsettings/iso", c.iso.toString)
      _ <- setConfig("/capturesettings/aperture", c.aperture.toString)
  } yield ()

  private def setConfig(key: String, value: String): IO[Unit] =
    acquireRootWidget.map(rootWidget => {
      rootWidget.setValue(key, value)
      rootWidget.apply()
      CameraUtils.closeQuietly(rootWidget)
    })
}

object CameraService {

  case class Capture(shutterSpeed: Double, shutterSpeedString: String, iso: Int, aperture: Double)
  object Capture {
    def apply(shutterSpeed: Double, iso: Int, aperture: Double): Option[Capture] =
      ShutterSpeedMap.toShutterSpeed(shutterSpeed).map(sss => {
        new Capture(shutterSpeed, sss, iso, aperture)
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