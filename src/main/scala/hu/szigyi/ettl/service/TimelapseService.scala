package hu.szigyi.ettl.service

import java.time.{Clock, ZoneOffset, ZonedDateTime}
import java.util.UUID

import cats.effect.{IO, Timer}
import hu.szigyi.ettl.service.CameraService.CameraError
import hu.szigyi.ettl.util.{ShellKill, ShutterSpeedMap}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.InfluxDbClient
import hu.szigyi.ettl.client.InfluxDbClient.TimelapseTask
import hu.szigyi.ettl.service.Scale.ScaledSetting

import scala.concurrent.duration._

// TODO: Use proper scheduler from BlazeServer to emit events!
class TimelapseService(shellKill: ShellKill, influx: InfluxDbClient[IO], rateLimit: Duration,
                       clock: Clock)(implicit timer: Timer[IO]) extends StrictLogging {

  def storeTestTimelapseTask: IO[Unit] = {
    val scaled = Scale.scale(Curvature.settings.reverse, ZonedDateTime.now())
    val delayInMillisec = 500
    val now = clock.instant()
    val start = now.plusSeconds(1)
    val end = start.plusMillis(scaled.size * delayInMillisec)
    val time = Iterator.iterate(start)(_.plusMillis(delayInMillisec)).takeWhile(_.isBefore(end)).toSeq
    logger.info(scaled.mkString("\n"))
    logger.info(time.toSeq.mkString("\n"))
    val id = UUID.randomUUID().toString
    val task = time.zip(scaled).map {
      case (time, scaled) =>
        import TimelapseTask._
        TimelapseTask(time, id, true, now, scaled.shutterSpeed, scaled.iso, scaled.aperture)
    }
    influx.writeTimelapseTasks(task).map(_ => logger.info(s"Stored: $task"))
  }

  def doTask: IO[Unit] = {
    val now = clock.instant()
    val end = now.plusNanos(rateLimit.toNanos)
    influx.getTimelapseTasks(now, end).map(_.toList).map {
      case Nil =>
        IO.pure(Right("No tasks to run!"))
      case t :: _ =>
        val cameraService = new CameraService(shellKill)
        logger.info(t.toString)
        val scaled = ScaledSetting(
          t.timestamp.time.atZone(ZoneOffset.UTC),
          t.shutterSpeed,
          ShutterSpeedMap.toShutterSpeed(t.shutterSpeed).get,
          t.iso,
          t.aperture
        )
        cameraService.useCamera(setSettings(cameraService, Seq(scaled)))
    }
  }

  private def setSettings(cameraService: CameraService, scaled: Seq[Scale.ScaledSetting]): IO[List[Unit]] =
    scaled.toList.traverse(setting => {
      cameraService.setEvSettings(setting.shutterSpeedString, setting.iso.toString, setting.aperture.toString)
    })

  def test: IO[Either[CameraError, String]] = {
    val cameraService = new CameraService(shellKill)
    val scaled = Scale.scale(Curvature.settings.reverse, ZonedDateTime.now())
    cameraService.useCamera(setSettings(cameraService, scaled))
  }
}
