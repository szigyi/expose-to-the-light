package hu.szigyi.ettl.v1

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v1.service.CameraService
import org.gphoto2.CameraFile

import java.time.Clock
import scala.concurrent.duration.DurationInt

object ShutterTestApp extends IOApp with StrictLogging {

  override def run(args: List[String]): IO[ExitCode] = {
    val cam: CameraService = new CameraService(Clock.systemUTC())
    (for {
      _ <- IO(cam.initialise)
      img <- doItAgain(cam)
    } yield img).as(ExitCode.Success)
  }

  def doItAgain(cam: CameraService): IO[Seq[CameraFile]] =
    (0 until 10).toList.traverse { e =>
      IO.sleep(1.second).flatMap(_ => cam.captureImage)
    }
}
