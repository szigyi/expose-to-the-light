package hu.szigyi.ettl.service

import hu.szigyi.ettl.testing.GCameraFixture._
import hu.szigyi.ettl.hal.{GCamera, GConfiguration, GFile}
import org.gphoto2.GPhotoException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Path
import scala.util.{Failure, Success, Try}

class CameraHandlerSpec extends AnyFreeSpec with Matchers {

  "connectToCamera" - {

    "normal scenario" - {
      "connect to the camera and get back the configuration" in {
        var retried = false
        val config: Try[GConfiguration] = capturedConfiguration
        val handler: Try[GConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize: Try[Unit] = Try(())
          override def newConfiguration: Try[GConfiguration] = config
          override def captureImage: Try[GFile] = throw new UnsupportedOperationException()
          override def close: Try[Unit] = throw new UnsupportedOperationException()
        }, { retried = true })

        handler shouldBe a[Success[_]]
        handler shouldBe config
      }
    }

    "error scenario" - {
      "fails to connect to the camera if unhandled exception happens" in {
        var retried = false
        val handler: Try[GConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize: Try[Unit] = Failure(new GPhotoException("bad", -105))
          override def newConfiguration: Try[GConfiguration] = capturedConfiguration
          override def captureImage: Try[GFile] = throw new UnsupportedOperationException()
          override def close: Try[Unit] = throw new UnsupportedOperationException()
        }, { retried = true })

        handler shouldBe a[Failure[_]]
        retried shouldBe false
      }

      "retries to connect to the camera when the gphoto process is already running in the background" in {
        var count = 0
        var retried = false
        val handler: Try[GConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize: Try[Unit] =
            count match {
              case 0 =>
                count += 1
                Failure(new GPhotoException("bad", -53))
              case 1 =>
                Try(())
            }
          override def newConfiguration: Try[GConfiguration] = capturedConfiguration
          override def captureImage: Try[GFile] = throw new UnsupportedOperationException()
          override def close: Try[Unit] = throw new UnsupportedOperationException()
        }, { retried = true })

        handler shouldBe a[Success[_]]
        retried shouldBe true
      }
    }
  }

  "takePhoto" - {
    "take a photo and return the camera file" in {
      val cameraFile: Try[GFile] = CameraHandler.takePhoto(new GCamera {
        override def initialize: Try[Unit] = throw new UnsupportedOperationException()
        override def newConfiguration: Try[GConfiguration] = throw new UnsupportedOperationException()
        override def captureImage: Try[GFile] = Try(new GFile {
          override def close: Try[Unit] = throw new UnsupportedOperationException()
          override def saveImageTo(imageBasePath: Path): Try[Path] = throw new UnsupportedOperationException()
        })
        override def close: Try[Unit] = throw new UnsupportedOperationException()
      })

      cameraFile shouldBe a[Success[_]]
    }

    "return error when exception happens during capture" in {
      val cameraFile: Try[GFile] = CameraHandler.takePhoto(new GCamera {
        override def initialize: Try[Unit] = throw new UnsupportedOperationException()
        override def newConfiguration: Try[GConfiguration] = throw new UnsupportedOperationException()
        override def captureImage: Try[GFile] = Failure(new GPhotoException("cannot take photo", -999))
        override def close: Try[Unit] = throw new UnsupportedOperationException()
      })

      cameraFile shouldBe a[Failure[_]]
    }
  }
}
