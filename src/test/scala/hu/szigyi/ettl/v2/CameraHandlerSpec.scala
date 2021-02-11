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
        val config = capturedConfiguration
        val handler: Try[GCameraConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize(): Unit = ()
          override def newConfiguration(): GCameraConfiguration = config
          override def captureImage(): CameraFile = ???
        }, { retried = true })

        handler shouldBe a[Success[_]]
        handler.get shouldBe config
      }
    }

    "error scenario" - {
      "fails to connect to the camera if unhandled exception happens" in {
        var retried = false
        val handler: Try[GCameraConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize(): Unit = throw new GPhotoException("bad", -105)
          override def newConfiguration(): GCameraConfiguration = capturedConfiguration
          override def captureImage(): CameraFile = ???
        }, { retried = true })

        handler shouldBe a[Failure[_]]
        retried shouldBe false
      }

      "retries to connect to the camera when the gphoto process is already running in the background" in {
        var count = 0
        var retried = false
        val handler: Try[GCameraConfiguration] = CameraHandler.connectToCamera(new GCamera {
          override def initialize(): Unit =
            count match {
              case 0 =>
                count += 1
                throw new GPhotoException("bad", -53)
              case 1 =>
                ()
            }
          override def newConfiguration(): GCameraConfiguration = capturedConfiguration
          override def captureImage(): CameraFile = ???
        }, { retried = true })

        handler shouldBe a[Success[_]]
        retried shouldBe true
      }
    }
  }
}

object CameraHandlerSpec {
  def capturedConfiguration: GCameraConfiguration = new GCameraConfiguration {
    var map: Map[String, Any] = Map.empty

    override def getNames: Seq[String] = map.keys.toSeq

    override def setValue(name: String, value: Any): Unit =
      map = map + (name -> value)

    override def apply(): Unit = ()

    override def close: Unit = ()
  }
}
