package hu.szigyi.ettl.v1.service

import java.time.{Clock, Instant}

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v1.service.CameraService.{CameraError, CapturedCameraModel, GenericCameraError, OfflineCamera, SettingsCameraModel, parseFractionDouble}
import hu.szigyi.ettl.v1.util.{ShellKill, ShutterSpeedMap}
import org.gphoto2.{CameraMod, CameraFileMod, CameraUtils, CameraWidgets, GPhotoException}

import scala.util.{Failure, Try}

class CameraService(clock: Clock) extends StrictLogging {
  private val camera = new CameraMod()

  def initialise(): Unit = {
    camera.initialize()
    val conf = camera.newConfiguration()
    logger.trace("Getting settings names:")
    logger.trace(conf.getNames.toString)
    val imageFormat = "RAW"
    conf.setValue("/settings/capturetarget", "Memory card")
    conf.setValue("/imgsettings/imageformat", imageFormat)
    conf.setValue("/imgsettings/imageformatsd", imageFormat)
    conf.setValue("/capturesettings/drivemode", "Single silent")
    conf.apply()
    CameraUtils.closeQuietly(conf)
  }

  def recoverInitialise: Try[Unit] = {
    Try(initialise()).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        ShellKill.killGPhoto2Processes()
        Try(camera.newConfiguration())
      case unrecoverableException =>
        Failure(unrecoverableException)
    }
  }

  def useCamera(restOfTheApp: CameraWidgets => IO[Option[CapturedCameraModel]]): IO[Either[CameraError, Option[CapturedCameraModel]]] = {
    IO.fromTry(recoverInitialise)
      .flatMap(_ => restOfTheApp(camera.newConfiguration()))
      .attempt.map {
      case Right(captured) =>
        logger.debug(s"Task is done: $captured")
        Right(captured)
      case Left(e: GPhotoException) if e.result == -105 =>
        val error = OfflineCamera(e.getMessage)
        logger.debug(error.msg)
        Left(error)
      case Left(e: GPhotoException) =>
        logger.debug(e.getMessage, e)
        Left(GenericCameraError(e.getMessage, e.result, None))
      case Left(e) =>
        logger.debug(e.getMessage, e)
        Left(GenericCameraError(e.getMessage, 0, None))
    }.map(res => {
      CameraUtils.closeQuietly(camera)
      res
    })
  }

  def setEvSettings(rootWidget: CameraWidgets, c: SettingsCameraModel): IO[Unit] =
    IO.fromTry(Try {
      logger.info(s"[ss: ${c.shutterSpeedString}, i: ${c.iso}, a: ${c.aperture}]")
      c.shutterSpeedString.foreach(rootWidget.setValue("/capturesettings/shutterspeed", _))
      c.iso.foreach(iso => rootWidget.setValue("/imgsettings/iso", iso.toString))
      c.aperture.foreach(aperture => rootWidget.setValue("/capturesettings/aperture", aperture.toString))
      val isAnyValueHasBeenSet = Seq(c.shutterSpeedString, c.iso.map(_.toString), c.aperture.map(_.toString)).flatten
      if (isAnyValueHasBeenSet.nonEmpty) rootWidget.apply()
    })

  def getSettings(rootWidget: CameraWidgets): IO[CapturedCameraModel] =
    IO.fromTry(Try {
      CapturedCameraModel(
        clock.instant(),
        parseFractionDouble(rootWidget.getValue("/capturesettings/shutterspeed").toString),
        rootWidget.getValue("/capturesettings/shutterspeed").toString,
        rootWidget.getValue("/imgsettings/iso").toString.toInt,
        rootWidget.getValue("/capturesettings/aperture").toString.toDouble
      )
    })

  def captureImage: IO[CameraFileMod] =
    IO.fromTry(Try {
      val cameraFile = camera.captureImage()
      CameraUtils.closeQuietly(cameraFile)
      cameraFile
    })
}

object CameraService {

  case class SettingsCameraModel(shutterSpeed: Option[Double], shutterSpeedString: Option[String], iso: Option[Int],
                                 aperture: Option[Double])
  case class CapturedCameraModel(time: Instant, shutterSpeed: Double, shutterSpeedString: String, iso: Int,
                                 aperture: Double)

  object SettingsCameraModel {
    def apply(shutterSpeed: Option[Double], iso: Option[Int], aperture: Option[Double]): SettingsCameraModel =
      new SettingsCameraModel(shutterSpeed, shutterSpeed.flatMap(ShutterSpeedMap.toShutterSpeed), iso, aperture)
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

  private def parseFractionDouble(s: String): Double = {
    s.toDoubleOption match {
      case Some(value) => value
      case None =>
        s.split("/").toList match {
          case Nil => throw new Exception(s"Cannot parse $s as double")
          case numeratorS :: denominatorS :: Nil =>
            val numerator = numeratorS.toDouble
            val denominator = denominatorS.toDouble
            numerator / denominator
          case _ => throw new NumberFormatException(s"Cannot parse '$s' as Double")
        }
    }
  }
}