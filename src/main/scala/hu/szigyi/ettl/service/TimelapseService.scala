package hu.szigyi.ettl.service

import java.time.ZonedDateTime

import cats.effect.{ContextShift, IO, Timer}
import org.gphoto2.CameraWidgets

import scala.concurrent.{ExecutionContext, Future}

// TODO: Use proper scheduler from BlazeServer to emit events!
class TimelapseService(cameraService: CameraService)(implicit ec: ExecutionContext, cs: ContextShift[IO]) {

  private def setSettings(scaled: Seq[Scale.ScaledSetting])(rootWidget: CameraWidgets): Unit =
    scaled.map(setting => {
      cameraService.setSettings(rootWidget, setting.shutterSpeed, setting.iso, setting.aperture)
      Thread.sleep(500)
    }).reduce((_, _) => ())

  def test: IO[Unit] = {
    val scaled = Scale.scale(Curvature.settings.reverse, ZonedDateTime.now())
    IO.fromFuture(IO(Future {
      cameraService.useCamera(setSettings(scaled))
    }(ec)))
  }
}
