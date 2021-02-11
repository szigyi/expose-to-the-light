package hu.szigyi.ettl

import java.time.Clock

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
//import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.CameraService
import hu.szigyi.ettl.util.ShellKill
import org.gphoto2.CameraFile

import scala.concurrent.duration._

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
