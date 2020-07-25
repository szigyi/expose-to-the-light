package hu.szigyi.ettl.service

import java.time.{Clock, Instant}

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService.{CameraError, CaptureCameraModel, CapturedCameraModel, GenericCameraError, OfflineCamera}
import hu.szigyi.ettl.util.{ShellKill, ShutterSpeedMap}
import org.gphoto2.{Camera, CameraUtils, CameraWidgets, GPhotoException}

import scala.util.{Failure, Try}

class CameraService(kill: ShellKill, clock: Clock) extends StrictLogging {
  private val camera = new Camera()

  private def initialise: Unit = {
    camera.initialize()
    val conf = camera.newConfiguration()
    logger.info("Getting settings names:")
    logger.info(conf.getNames.toString)
    val imageFormat = "RAW"
    conf.setValue("/settings/capturetarget", "Memory card")
    conf.setValue("/imgsettings/imageformat", imageFormat)
    conf.setValue("/imgsettings/imageformatsd", imageFormat)
    conf.setValue("/capturesettings/drivemode", "Single silent")
    conf.apply()
    CameraUtils.closeQuietly(conf)
  }

  def useCamera(restOfTheApp: CameraWidgets => IO[CapturedCameraModel]): IO[Either[CameraError, CapturedCameraModel]] = {
    IO.fromTry(Try(initialise).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        kill.killGPhoto2Processes
        Try(camera.newConfiguration())
      case unrecoverableException =>
        Failure(unrecoverableException)

    }).flatMap(_ => restOfTheApp(camera.newConfiguration())).attempt.map {
      case Right(captured) =>
        logger.debug(s"Task is done: $captured")
        Right(captured)
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

  def setEvSettings(rootWidget: CameraWidgets, c: CaptureCameraModel): IO[CapturedCameraModel] =
    IO.fromTry(Try {
      logger.info(s"[ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]")
      rootWidget.setValue("/capturesettings/shutterspeed", c.shutterSpeedString)
      rootWidget.setValue("/imgsettings/iso", c.iso.toString)
      rootWidget.setValue("/capturesettings/aperture", c.aperture.toString)
      rootWidget.apply()
    })
      .flatMap(_ => getSettings(rootWidget))

  private def getSettings(rootWidget: CameraWidgets): IO[CapturedCameraModel] =
    IO.fromTry(Try {
      CapturedCameraModel(
        clock.instant(),
        rootWidget.getValue("/capturesettings/shutterspeed").toString.toDouble,
        rootWidget.getValue("/capturesettings/shutterspeed").toString,
        rootWidget.getValue("/imgsettings/iso").toString.toInt,
        rootWidget.getValue("/capturesettings/aperture").toString.toDouble
      )
    })

  def captureImage: IO[Unit] = {
    IO.fromTry(Try {
      val cameraFile = camera.captureImage()
      CameraUtils.closeQuietly(cameraFile)
    })
  }
}

object CameraService {

  case class CaptureCameraModel(shutterSpeed: Option[Double], shutterSpeedString: Option[String], iso: Option[Int],
                                aperture: Option[Double])
  case class CapturedCameraModel(time: Instant, shutterSpeed: Double, shutterSpeedString: String, iso: Int,
                                 aperture: Double)
  object CaptureCameraModel {
    def apply(shutterSpeed: Option[Double], iso: Option[Int], aperture: Option[Double]): Option[CaptureCameraModel] =
      shutterSpeed.map(ss => {
        new CaptureCameraModel(shutterSpeed, ShutterSpeedMap.toShutterSpeed(ss), iso, aperture)
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
        |""".stripMargin.replaceAll("\n", " "))
  }
  case class GenericCameraError(msg: String, result: Int, suggestion: Option[String]) extends CameraError
}