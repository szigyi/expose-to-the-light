package hu.szigyi.ettl.v1.tools

import java.time.Clock

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v1.service.CameraService
import hu.szigyi.ettl.v1.service.CameraService.{CapturedCameraModel, SettingsCameraModel}
import hu.szigyi.ettl.v1.util.ShellKill
import org.gphoto2.CameraWidgets
import ch.qos.logback.classic.{Level,Logger}
import org.slf4j.LoggerFactory

object CompareCameraAccessStylesApp extends IOApp with StrictLogging {

  LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).
    asInstanceOf[Logger].setLevel(Level.DEBUG)

  override def run(args: List[String]): IO[ExitCode] = {
    val clock = Clock.systemUTC()

    val cameraService = new CameraService(clock)
    val s = SettingsCameraModel(None, None, None)

    val r = cameraService.useCamera(captureTask(cameraService, s))

    r.as(ExitCode.Success)
  }

  private def captureTask(cameraService: CameraService,
                          settings: SettingsCameraModel)(rootWidget: CameraWidgets): IO[Option[CapturedCameraModel]] = {
    for {
      _ <- cameraService.setEvSettings(rootWidget, settings)
      _ <- cameraService.captureImage
      cm <- cameraService.getSettings(rootWidget)
    } yield Some(cm)
  }
}
