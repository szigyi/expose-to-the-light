package hu.szigyi.ettl.service

import java.time.{Clock, Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

import cats.effect.{IO, Timer}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.client.influx.InfluxDomain.{Captured, TimelapseTask}
import hu.szigyi.ettl.service.CameraService.{CaptureCameraModel, CapturedCameraModel}
import hu.szigyi.ettl.util.ShellKill
import org.gphoto2.CameraWidgets

import scala.concurrent.duration._

class TimelapseService(shellKill: ShellKill, influx: InfluxDbClient[IO], rateLimit: Duration,
                       clock: Clock)(implicit timer: Timer[IO]) extends StrictLogging {

  def storeTimelapseTask(keyFrameId: String, startAt: Instant): IO[Seq[TimelapseTask]] =
  for {
    now       <- IO.pure(clock.instant())
    keyFrames <- influx.getKeyFrames(keyFrameId)
    scaled    = Scale.scaleKeyFrames(keyFrames.reverse, startAt.atZone(ZoneOffset.UTC))
    id        = UUID.randomUUID().toString
    tasks     = scaled.map(s => {
      import TimelapseTask._
      TimelapseTask(s.time.toInstant, id, false, now, Some(s.shutterSpeed), Some(s.iso), Some(s.aperture),
        Some(EvService.ev(s.shutterSpeed, s.iso, s.aperture)))
    })
    _         <- influx.writeTimelapseTasks(tasks)
      .map(_ => logger.debug(s"Stored: \n${tasks.mkString("\n")}"))
  } yield tasks

  def storeTestTimelapseTask(keyFrameId: String): IO[Seq[TimelapseTask]] =
  for {
    keyFrames         <- influx.getKeyFrames(keyFrameId)
    artificialSunset  = ZonedDateTime.now().plusHours(3)
    scaled            = Scale.scaleKeyFrames(keyFrames.reverse, artificialSunset)
    delayInMillisec   = 1000
    now               = clock.instant()
    start             = now.plusSeconds(1)
    end               = start.plusMillis(scaled.size * delayInMillisec)
    time              = Iterator.iterate(start)(_.plusMillis(delayInMillisec)).takeWhile(_.isBefore(end)).toSeq
    id                = UUID.randomUUID().toString
    tasks             = time.zip(scaled).map {
      case (time, scaled) =>
        import TimelapseTask._
        TimelapseTask(time, id, true, now, Some(scaled.shutterSpeed), Some(scaled.iso), Some(scaled.aperture),
          Some(EvService.ev(scaled.shutterSpeed, scaled.iso, scaled.aperture)))
    }
    _                 <- influx.writeTimelapseTasks(tasks)
      .map(_ => logger.debug(s"Stored: \n${tasks.mkString("\n")}"))
  } yield tasks

  def executeCurrentTimelapseTasks: IO[Unit] =
    for {
      from <- IO(clock.instant())
      to <- IO(from.plusMillis(rateLimit.toMillis))
      maybeTask <- getTimelapseTasks(from, to).map(_.headOption)
      _ <- IO(maybeTask.map(task => logger.info(s"Executing task: $task")))
      - <- executeTaskOnCamera(maybeTask)
    } yield ()

  def getCapturedTasks(from: Instant): IO[Seq[Captured]] =
    influx.getCaptured(from).map(_.toSeq)

  private def getTimelapseTasks(from: Instant, to: Instant): IO[Seq[TimelapseTask]] =
    influx.getTimelapseTasks(from, to).map(_.toSeq)

  private def executeTaskOnCamera(maybeTask: Option[TimelapseTask]): IO[Option[Unit]] = {
//    for {
//      task <- IO.fromEither(Either.fromOption(maybeTask))
//      capture <- Capture(task.shutterSpeed, task.iso, task.aperture)
//    } yield None

    maybeTask.flatMap(s => {
      CaptureCameraModel(s.shutterSpeed, s.iso, s.aperture).map(c => {
        val cameraService = new CameraService(shellKill, clock)
        cameraService.useCamera(executeTask(cameraService, c, captureImageIfNotTest(s.test))).flatMap {
          case Right(c) =>
            import Captured._
            val captured = Captured(c.time, s.id, s.test, c.shutterSpeed, c.iso, c.aperture,
              EvService.ev(c.shutterSpeed, c.iso, c.aperture), None, None)
            influx.writeCaptured(Seq(captured))
          case Left(cameraError) =>
            import Captured._
            val captured = Captured(clock.instant(), s.id, s.test, s.shutterSpeed.getOrElse(0.0d),
              s.iso.getOrElse(0), s.aperture.getOrElse(0.0d), s.ev.getOrElse(0.0d),
              Some(cameraError.msg), cameraError.suggestion)
            influx.writeCaptured(Seq(captured))
        }
      })
    }).sequence
  }

  private def captureImageIfNotTest(test: Boolean): Boolean = !test

  private def executeTask(cameraService: CameraService, capture: CaptureCameraModel, isCapture: Boolean)(rootWidget: CameraWidgets): IO[CapturedCameraModel] = {
    for {
      captured  <- cameraService.setEvSettings(rootWidget, capture)
      _         <- if (isCapture) cameraService.captureImage else IO.unit
    } yield captured
  }
}
