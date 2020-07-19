package hu.szigyi.ettl.service

import java.time.{Clock, Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

import cats.data.OptionT
import cats.effect.{IO, Timer}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.client.influx.InfluxDomain.{Captured, TimelapseTask}
import hu.szigyi.ettl.service.CameraService.{CameraError, Capture}
import hu.szigyi.ettl.service.Scale.ScaledSetting
import hu.szigyi.ettl.util.{ShellKill, ShutterSpeedMap}

import scala.concurrent.duration._

class TimelapseService(shellKill: ShellKill, influx: InfluxDbClient[IO], rateLimit: Duration,
                       clock: Clock)(implicit timer: Timer[IO]) extends StrictLogging {

  def storeTestTimelapseTask: IO[Seq[TimelapseTask]] = {
    val scaled = Scale.scale(Curvature.settings.reverse, ZonedDateTime.now().plusHours(3))
    val delayInMillisec = 2000
    val now = clock.instant()
    val start = now.plusSeconds(1)
    val end = start.plusMillis(scaled.size * delayInMillisec)
    val time = Iterator.iterate(start)(_.plusMillis(delayInMillisec)).takeWhile(_.isBefore(end)).toSeq
    val id = UUID.randomUUID().toString
    val tasks: Seq[TimelapseTask] = time.zip(scaled).map {
      case (time, scaled) =>
        import TimelapseTask._
        TimelapseTask(time, id, true, now, scaled.shutterSpeed, scaled.iso, scaled.aperture,
          EvService.ev(scaled.iso, scaled.shutterSpeed, scaled.aperture))
    }
    influx.writeTimelapseTasks(tasks)
      .map(_ => logger.debug(s"Stored: \n${tasks.mkString("\n")}"))
      .map(_ => tasks)
  }

  def executeCurrentTimelapseTasks: IO[Unit] =
    for {
      from <- IO(clock.instant())
      to <- IO(from.plusMillis(rateLimit.toMillis))
      maybeTask <- getTimelapseTasks(from, to).map(_.headOption)
      - <- executeTaskOnCamera(maybeTask)
    } yield ()

  def getCapturedTasks(from: Instant): IO[Seq[Captured]] =
    influx.getCaptured(from).map(_.toList)

  private def getTimelapseTasks(from: Instant, to: Instant): IO[Seq[TimelapseTask]] =
    influx.getTimelapseTasks(from, to).map(_.toList)

  private def executeTaskOnCamera(maybeTask: Option[TimelapseTask]): IO[Option[Unit]] = {
//    for {
//      task <- IO.fromEither(Either.fromOption(maybeTask))
//      capture <- Capture(task.shutterSpeed, task.iso, task.aperture)
//    } yield None

    maybeTask.flatMap(s => {
      Capture(s.shutterSpeed, s.iso, s.aperture).map(c => {
        val cameraService = new CameraService(shellKill)
        cameraService.useCamera(setSettings(cameraService, Seq(c))).flatMap {
          case Right(captured) =>
            import Captured._
            val captured = Captured(clock.instant(), s.id, s.test, s.shutterSpeed, s.iso, s.aperture, s.ev, None, None)
            influx.writeCaptured(Seq(captured))
          case Left(cameraError) =>
            import Captured._
            val captured = Captured(clock.instant(), s.id, s.test, s.shutterSpeed, s.iso, s.aperture, s.ev,
              Some(cameraError.msg), cameraError.suggestion)
            influx.writeCaptured(Seq(captured))
        }
      })
    }).sequence
  }

  private def setSettings(cameraService: CameraService, captures: Seq[Capture]): IO[List[Unit]] =
    captures.toList.traverse(capture => {
      cameraService.setEvSettings(capture)
    })

  def test: IO[Either[CameraError, String]] = {
    val cameraService = new CameraService(shellKill)
    val scaled = Scale.scale(Curvature.settings.reverse, ZonedDateTime.now())
    ???
//    cameraService.useCamera(setSettings(cameraService, scaled))
  }
}
