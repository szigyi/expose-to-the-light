package hu.szigyi.ettl.v2

import hu.szigyi.ettl.v1.service.CameraService.SettingsCameraModel
import hu.szigyi.ettl.v2.app.CliApp.AppConfiguration
import hu.szigyi.ettl.v2.SchedulerFixture.immediateScheduler
import hu.szigyi.ettl.v2.hal.{DummyCamera, GCamera, GConfiguration, GFile}
import hu.szigyi.ettl.v2.service.EttlApp
import org.gphoto2.GPhotoException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class EttlAppSpec extends AnyFreeSpec with Matchers {

  "runEttl" - {
    "Normal Scenario" - {
      "set camera then capture images with custom settings and returns image paths" in {
        val camera = new DummyCamera(testing = true)
        val result = new EttlApp(AppConfiguration(Paths.get("/"), "CR2"), camera, immediateScheduler)
          .execute(Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))), 2, 10.millisecond)

        result shouldBe a[Success[_]]
        result.get shouldBe Seq(Paths.get("/IMG_0001.JPG"), Paths.get("/IMG_0002.JPG"))
        camera.adjustedCameraSettings.keys.toSeq should contain theSameElementsAs Seq(
          "/imgsettings/imageformatsd",
          "/imgsettings/iso",
          "/imgsettings/imageformat",
          "/capturesettings/drivemode",
          "/settings/capturetarget",
          "/capturesettings/shutterspeed",
          "/capturesettings/aperture"
        )
        camera.savedImages.map(_.toString) should contain theSameElementsAs Seq(
          "/IMG_0001.JPG", "/IMG_0002.JPG"
        )
      }

      "set camera then capture images and returns image paths" in {
        val camera = new DummyCamera(testing = true)
        val result = new EttlApp(AppConfiguration(Paths.get("/"), "CR2"), camera, immediateScheduler)
          .execute(None,2, 10.millisecond)

        result shouldBe a[Success[_]]
        result.get shouldBe Seq(Paths.get("/IMG_0001.JPG"), Paths.get("/IMG_0002.JPG"))
        camera.adjustedCameraSettings.keys.toSeq should contain theSameElementsAs Seq(
          "/imgsettings/imageformatsd",
          "/imgsettings/imageformat",
          "/capturesettings/drivemode",
          "/settings/capturetarget",
        )
      }
    }

    "Error Scenario" - {
      "when cannot connect to the camera then fast fail and return error" in {
        val result = new EttlApp(AppConfiguration(Paths.get("/"), "CR2"), new GCamera {
          override def initialize: Try[Unit] =
            Failure(new GPhotoException("gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model", -105))

          override def newConfiguration: Try[GConfiguration] = throw new UnsupportedOperationException()

          override def captureImage: Try[GFile] = throw new UnsupportedOperationException()
        }, immediateScheduler).execute(None, 1, 10.millisecond)

        result shouldBe a[Failure[_]]
        result.failed.get.getMessage shouldBe "gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model"
      }

      "when capture settings are invalid and image cannot be taken" ignore {

      }
    }
  }
}
