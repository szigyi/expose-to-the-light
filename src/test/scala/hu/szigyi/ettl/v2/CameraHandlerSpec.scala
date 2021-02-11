package hu.szigyi.ettl.v2

import hu.szigyi.ettl.v2.CameraHandlerSpec.capturedConfiguration
import org.gphoto2.{CameraFile, CameraWidgets, GPhotoException}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class CameraHandlerSpec extends AnyFreeSpec with Matchers {

  "connectToCamera" - {

    "normal scenario" - {
      "connect to the camera and get back the configuration" in {
        var retried = false
        val config: Try[GConfiguration] = capturedConfiguration
        val handler: Try[GConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize(): Try[Unit] = Try()
          override def newConfiguration(): Try[GConfiguration] = config
          override def captureImage(): Try[GFile] = ???
        }, { retried = true })

        handler shouldBe a[Success[_]]
        handler shouldBe config
      }
    }

    "error scenario" - {
      "fails to connect to the camera if unhandled exception happens" in {
        var retried = false
        val handler: Try[GConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize(): Try[Unit] = Failure(new GPhotoException("bad", -105))
          override def newConfiguration(): Try[GConfiguration] = capturedConfiguration
          override def captureImage(): Try[GFile] = ???
        }, { retried = true })

        handler shouldBe a[Failure[_]]
        retried shouldBe false
      }

      "retries to connect to the camera when the gphoto process is already running in the background" in {
        var count = 0
        var retried = false
        val handler: Try[GConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize(): Try[Unit] =
            count match {
              case 0 =>
                count += 1
                Failure(new GPhotoException("bad", -53))
              case 1 =>
                Try()
            }
          override def newConfiguration(): Try[GConfiguration] = capturedConfiguration
          override def captureImage(): Try[GFile] = ???
        }, { retried = true })

        handler shouldBe a[Success[_]]
        retried shouldBe true
      }
    }
  }

  "takePhoto" - {
    "take a photo and return the camera file" in {
      val cameraFile: Try[GFile] = CameraHandler.takePhoto(new GCamera {
        override def initialize(): Try[Unit] = ???
        override def newConfiguration(): Try[GConfiguration] = ???
        override def captureImage(): Try[GFile] = Try(new GFile {
          override def close: Unit = ???
          override def getImage: Try[Array[Byte]] = ???
        })
      })

      cameraFile shouldBe a[Success[_]]
    }

    "return error when exception happens during capture" in {
      val cameraFile: Try[GFile] = CameraHandler.takePhoto(new GCamera {
        override def initialize(): Try[Unit] = ???
        override def newConfiguration(): Try[GConfiguration] = ???
        override def captureImage(): Try[GFile] = Failure(new GPhotoException("cannot take photo", -999))
      })

      cameraFile shouldBe a[Failure[_]]
    }
  }
}

object CameraHandlerSpec {
  def capturedConfiguration: Try[GConfiguration] = Try(new GConfiguration {
    var map: Map[String, Any] = Map.empty

    override def getNames: Seq[String] = map.keys.toSeq

    override def setValue(name: String, value: Any): Unit =
      map = map + (name -> value)

    override def apply(): Unit = ()

    override def close: Unit = ()
  })
}
