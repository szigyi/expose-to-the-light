package hu.szigyi.ettl.service

import java.time.{Clock, Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

import cats.effect.{IO, Timer}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.client.influx.InfluxDomain.{CapturedDomain, ToCaptureDomain, ToSetSettingDomain}
import hu.szigyi.ettl.service.CameraService.{CameraError, CapturedCameraModel, SettingsCameraModel}
import hu.szigyi.ettl.util.ShellKill
import org.gphoto2.{CameraFile, CameraWidgets}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class TimelapseService(shellKill: ShellKill, influx: InfluxDbClient[IO], capturedImageService: CapturedImageService,
                       rateLimit: Duration, clock: Clock)(implicit timer: Timer[IO]) extends StrictLogging {

  def storeSettings(keyFrameId: String, startAt: Instant): IO[Seq[ToSetSettingDomain]] =
  for {
    now       <- IO.pure(clock.instant())
    keyFrames <- influx.getKeyFrames(keyFrameId)
    scaled    = Scale.scaleKeyFrames(keyFrames.reverse, startAt.atZone(ZoneOffset.UTC))
    id        = UUID.randomUUID().toString
    tasks     = scaled.map(s => {
      import ToSetSettingDomain._
      ToSetSettingDomain(s.time.toInstant, id, false, now, Some(s.shutterSpeed), Some(s.iso), Some(s.aperture),
        Some(EvService.ev(s.shutterSpeed, s.iso, s.aperture)))
    })
    _         <- influx.writeSettings(tasks)
      .map(_ => logger.debug(s"Stored: \n${tasks.mkString("\n")}"))
  } yield tasks

  def storeTestSettings(keyFrameId: String): IO[Seq[ToSetSettingDomain]] =
    for {
      keyFrames         <- influx.getKeyFrames(keyFrameId)
      startAt           = clock.instant().atZone(ZoneOffset.UTC)
      scaled            = Scale.scaleKeyFrames(keyFrames.reverse, startAt)
      delayInMillisec   = 1000
      now               = clock.instant()
      start             = now.plusSeconds(1)
      end               = start.plusMillis(scaled.size * delayInMillisec)
      time              = Iterator.iterate(start)(_.plusMillis(delayInMillisec)).takeWhile(_.isBefore(end)).toSeq
      id                = UUID.randomUUID().toString
      tasks             = time.zip(scaled).map {
        case (time, scaled) =>
          import ToSetSettingDomain._
          ToSetSettingDomain(time, id, true, now, Some(scaled.shutterSpeed), Some(scaled.iso), Some(scaled.aperture),
            Some(EvService.ev(scaled.shutterSpeed, scaled.iso, scaled.aperture)))
      }
      _                 <- influx.writeSettings(tasks)
        .map(_ => logger.debug(s"Stored: \n${tasks.mkString("\n")}"))
    } yield tasks

  def storeCaptureTicks(intervalSeconds: Int, count: Int, startAt: Instant): IO[Seq[ToCaptureDomain]] =
    for {
      ticks <- IO.pure(TickService.ticking(UUID.randomUUID().toString, intervalSeconds, count, startAt))
      _     <- influx.writeToBeCaptured(ticks)
    } yield ticks

  def executeCurrentTask: IO[Unit] =
    for {
      from          <- IO(clock.instant())
      to            <- IO(from.plusMillis(rateLimit.toMillis))
      maybeSettings <- getSettings(from, to).map(_.headOption)
      maybeTicks    <- getTicks(from, to).map(_.headOption)
      _             <- executeTaskOnCamera(maybeSettings, maybeTicks)
    } yield ()

  def getCapturedMetaData(from: Instant): IO[Seq[CapturedDomain]] =
    influx.getCaptured(from).map(_.toSeq)

  private def getSettings(from: Instant, to: Instant): IO[Seq[ToSetSettingDomain]] =
    influx.getSettings(from, to).map(_.toSeq)

  private def getTicks(from: Instant, to: Instant): IO[Seq[ToCaptureDomain]] =
    influx.getTicks(from, to).map(_.toSeq)

  private def executeTaskOnCamera(maybeSettings: Option[ToSetSettingDomain],
                                  maybeTicks: Option[ToCaptureDomain]): IO[Unit] = {
    (maybeSettings, maybeTicks) match {
      case (None, None) => // Carry on! Nothing to see here!
        logger.trace("Carry on! Nothing to see here!")
        IO.unit

      case (None, Some(t)) => // only capture image
        logger.info(s"Capture Only: $t")
        val cameraService = new CameraService(shellKill, clock)
        for {
          res <- cameraService.useCamera(executeCaptureTick(cameraService))
          _   <- storeTask(None, Some(t), res)
        } yield ()

      case (Some(s), None) => // only set new settings
        logger.info(s"Settings Only: $s")
        val cameraService = new CameraService(shellKill, clock)
        val c = SettingsCameraModel(s.shutterSpeed, s.iso, s.aperture)
        cameraService.useCamera(executeSettings(cameraService, c)) *>
          IO.unit

      case (Some(s), Some(t)) => // set new settings and capture image
        logger.info(s"Settings & Capture: $s - $t")
        val cameraService = new CameraService(shellKill, clock)
        val c = SettingsCameraModel(s.shutterSpeed, s.iso, s.aperture)
        for {
          res <- cameraService.useCamera(executeTasks(cameraService, c))
          _   <- storeTask(Some(s), Some(t), res)
        } yield ()
    }
  }

  private def executeTasks(cameraService: CameraService, settings: SettingsCameraModel)(rootWidget: CameraWidgets): IO[Option[CapturedCameraModel]] =
    for {
      _ <- executeSettings(cameraService, settings)(rootWidget)
      r <- executeCaptureTick(cameraService)(rootWidget)
    } yield r

  private def executeSettings(cameraService: CameraService, settings: SettingsCameraModel)(rootWidget: CameraWidgets): IO[Option[CapturedCameraModel]] =
    cameraService.setEvSettings(rootWidget, settings) *> IO.pure(None)

  private def executeCaptureTick(cameraService: CameraService)(rootWidget: CameraWidgets): IO[Option[CapturedCameraModel]] =
    for {
      cf  <- cameraService.captureImage
      r   <- readOutSettings(cameraService, rootWidget)
    } yield {
      Try(capturedImageService.saveCapturedImage(cf).unsafeRunSync()) match {
        case Success(value) => logger.info("Captured image have been stored!")
        case Failure(exception) => logger.error("Could not store captured image!", exception)
      }
      Some(r)
    }

  private def readOutSettings(cameraService: CameraService, rootWidget: CameraWidgets): IO[CapturedCameraModel] =
    cameraService.getSettings(rootWidget)

  private def storeTask(s: Option[ToSetSettingDomain], t: Option[ToCaptureDomain],
                        result: Either[CameraError, Option[CapturedCameraModel]]): IO[Unit] =
    result match {
      case Right(Some(c)) =>
        import CapturedDomain._
        val time = c.time
        val id = getOrDefault(s.map(_.id), t.map(_.id), "UNKNOWN")
        val test = getOrDefault(s.map(_.test), None, false)
        val order = getOrDefault(t.map(_.order), None, -1)
        val shutterSpeed = c.shutterSpeed
        val iso = c.iso
        val aperture = c.aperture
        val ev = EvService.ev(c.shutterSpeed, c.iso, c.aperture)
        val captured = CapturedDomain(time, id, test, order, shutterSpeed, iso, aperture, ev, None, None)
        logger.info(s"Storing: $captured")
        influx.writeCaptured(Seq(captured))

      case Right(None) =>
        logger.info("Not Storing Captured!")
        IO.unit

      case Left(cameraError) =>
        import CapturedDomain._
        val time = clock.instant()
        val id = getOrDefault(s.map(_.id), t.map(_.id), "UNKNOWN")
        val test = s.map(_.test).getOrElse(false)
        val order = t.map(_.order).getOrElse(-1)
        val shutterSpeed = s.flatMap(_.shutterSpeed).getOrElse(0.0d)
        val iso = s.flatMap(_.iso).getOrElse(0)
        val aperture = s.flatMap(_.aperture).getOrElse(0.0d)
        val ev = s.flatMap(_.ev).getOrElse(0.0d)
        val captured = CapturedDomain(time, id, test, order, shutterSpeed, iso, aperture, ev, Some(cameraError.msg), cameraError.suggestion)
        logger.info(s"Storing Error: $captured")
        influx.writeCaptured(Seq(captured))
    }

  private def getOrDefault[A](s: Option[A], t: Option[A], default: A): A =
    s.getOrElse(t.getOrElse(default))
}
