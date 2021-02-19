package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging
import org.gphoto2.GPhotoException

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}

object CameraHandler extends StrictLogging {

  def takePhoto(camera: GCamera): Try[GFile] =
    captureTimings(camera)

  def connectToCamera(camera: GCamera, shellKill: => Unit): Try[GConfiguration] =
    initialiseCamera(camera).recoverWith {
      case e: GPhotoException if e.result == -53 =>
        shellKill
        initialiseCamera(camera)
      case unrecoverableException =>
        Failure(unrecoverableException)
    }

  private def initialiseCamera(camera: GCamera): Try[GConfiguration] =
    camera.initialize.flatMap { _ =>
      camera.newConfiguration.map { configuration =>
        logger.trace("Getting settings names:")
        logger.trace(configuration.getNames.toString)
        val imageFormat = "RAW + Tiny JPEG"
        configuration.setValue("/settings/capturetarget", "Memory card")
        configuration.setValue("/imgsettings/imageformat", imageFormat)
        configuration.setValue("/imgsettings/imageformatsd", imageFormat)
        configuration.setValue("/capturesettings/drivemode", "Single silent")
        configuration.apply
        configuration
      }
    }

  private def captureTimings(camera: GCamera): Try[GFile] = {
    val started = System.nanoTime()
    val result = camera.captureImage
    val finished = System.nanoTime()
    val duration = Duration.apply(finished - started, TimeUnit.NANOSECONDS)
    logger.debug(s"Capture took: ${duration.toMillis}ms | ${duration.toNanos}ns")
    result
  }
}
