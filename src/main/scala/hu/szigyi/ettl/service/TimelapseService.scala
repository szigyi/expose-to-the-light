package hu.szigyi.ettl.service

import java.time.ZonedDateTime

import cats.effect.{ContextShift, IO, Timer}
import hu.szigyi.ettl.service.CameraService.CameraError
import hu.szigyi.ettl.util.ShellKill
import org.gphoto2.CameraWidgets

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// TODO: Use proper scheduler from BlazeServer to emit events!
class TimelapseService(shellKill: ShellKill)(implicit ec: ExecutionContext, cs: ContextShift[IO]) {

  private def setSettings(cameraService: CameraService, scaled: Seq[Scale.ScaledSetting])(rootWidget: => Try[CameraWidgets]): Try[Unit] =
    scaled.map(setting => {
      val res = cameraService.setSettings(rootWidget, setting.shutterSpeed, setting.iso, setting.aperture)
      Thread.sleep(500)
      res
    }).reduce((_, _) => Try(()))

  def test: IO[Either[CameraError, String]] = {
    val cameraService = new CameraService(shellKill)
    val scaled = Scale.scale(Curvature.settings.reverse, ZonedDateTime.now())
    IO.fromFuture(IO(Future {
      cameraService.useCamera(setSettings(cameraService, scaled))
    }(ec)))
  }
}
