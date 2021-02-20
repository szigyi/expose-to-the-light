package hu.szigyi.ettl.v2

import hu.szigyi.ettl.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.v2.CliApp.AppConfiguration
import hu.szigyi.ettl.v2.GCameraFixture.{capturedConfiguration, capturedFile}
import org.gphoto2.GPhotoException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class EttlAppSpec extends AnyFreeSpec with Matchers {

  "runEttl" - {
    "Normal Scenario" - {
      "set camera then capture image with custom settings and returns image paths" in {
        val configuration = capturedConfiguration
        val result = new EttlApp(AppConfiguration(Paths.get("/")), new GCamera {
          override def initialize: Try[Unit] = Try()
          override def newConfiguration: Try[GConfiguration] = configuration
          override def captureImage: Try[GFile] = capturedFile
        }).execute(
          Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))),
          2,
          1.nanosecond)

        result shouldBe a[Success[_]]
        result.get shouldBe Seq(Paths.get("/IMG_1.CR2"), Paths.get("/IMG_2.CR2"))
        configuration.get.getNames should contain theSameElementsAs Seq(
          "/imgsettings/imageformatsd",
          "/imgsettings/iso",
          "/imgsettings/imageformat",
          "/capturesettings/drivemode",
          "/settings/capturetarget",
          "/capturesettings/shutterspeed",
          "/capturesettings/aperture"
        )
      }

      "set camera then capture image and returns image paths" in {
        val configuration = capturedConfiguration
        val result = new EttlApp(AppConfiguration(Paths.get("/")), new GCamera {
          override def initialize: Try[Unit] = Try()
          override def newConfiguration: Try[GConfiguration] = configuration
          override def captureImage: Try[GFile] = capturedFile
        }).execute(None,2, 1.nanosecond)

        result shouldBe a[Success[_]]
        result.get shouldBe Seq(Paths.get("/IMG_1.CR2"), Paths.get("/IMG_2.CR2"))
        configuration.get.getNames should contain theSameElementsAs Seq(
          "/imgsettings/imageformatsd",
          "/imgsettings/imageformat",
          "/capturesettings/drivemode",
          "/settings/capturetarget",
        )
      }
    }

    "Error Scenario" - {
      "when cannot connect to the camera then fast fail and return error" in {
        val result = new EttlApp(AppConfiguration(Paths.get("/")), new GCamera {
          override def initialize: Try[Unit] =
            Failure(new GPhotoException("gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model", -105))

          override def newConfiguration: Try[GConfiguration] = ???

          override def captureImage: Try[GFile] = ???
        }).execute(None, 1, 1.nanosecond)

        result shouldBe a[Failure[_]]
        result.failed.get.getMessage shouldBe "gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model"
      }

      "when capture settings are invalid and image cannot be taken"
    }
  }
}
