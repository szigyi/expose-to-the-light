package hu.szigyi.ettl.service

import hu.szigyi.ettl.app.CliEttlApp.AppConfiguration
import hu.szigyi.ettl.hal.{DummyCamera, GCamera, GConfiguration, GFile}
import hu.szigyi.ettl.model.Model.SettingsCameraModel
import hu.szigyi.ettl.testing.SchedulerFixture.immediateScheduler
import org.gphoto2.GPhotoException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant.{parse => instant}
import java.nio.file.{Path, Paths}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class EttlAppSpec extends AnyFreeSpec with Matchers {

  "runEttl" - {
    "Normal Scenario" - {
      "set camera then capture images with custom settings" in {
        val camera = new DummyCamera(testing = true)
        val result = new EttlApp(
          AppConfiguration(Paths.get("/"), "CR2"), camera, immediateScheduler, new DirectoryService() {
            override def createFolder(folderPath: Path): Try[Unit] = Try(())
          }, instant("2021-03-19T19:00:00Z"))
          .execute(Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))), 2, 10.millisecond)

        result shouldBe a[Success[_]]
        result.get shouldBe Seq(Paths.get("/2021_03_19_19_00_00/IMG_0001.JPG"), Paths.get("/2021_03_19_19_00_00/IMG_0002.JPG"))
        camera.adjustedCameraSettings.keys.toSeq should contain theSameElementsAs Seq(
          "/imgsettings/iso",
          "/capturesettings/shutterspeed",
          "/capturesettings/aperture"
        )
      }

      "do not set camera then capture images without custom settings" in {
        val camera = new DummyCamera(testing = true)
        val result = new EttlApp(AppConfiguration(Paths.get("/"), "CR2"), camera, immediateScheduler, new DirectoryService {
          override def createFolder(folderPath: Path): Try[Unit] = Try(())
        }, instant("2021-03-19T19:00:00Z"))
          .execute(None,2, 10.millisecond)

        result shouldBe a[Success[_]]
        result.get shouldBe Seq(Paths.get("/2021_03_19_19_00_00/IMG_0001.JPG"), Paths.get("/2021_03_19_19_00_00/IMG_0002.JPG"))
        camera.adjustedCameraSettings.keys.toSeq should contain theSameElementsAs Seq.empty
      }
    }

    "Error Scenario" - {
      "when cannot connect to the camera then fast fail and return error" in {
        val result = new EttlApp(AppConfiguration(Paths.get("/"), "CR2"), new GCamera {
          override def initialize: Try[Unit] =
            Failure(new GPhotoException("gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model", -105))
          override def newConfiguration: Try[GConfiguration] = throw new UnsupportedOperationException()
          override def captureImage: Try[GFile] = throw new UnsupportedOperationException()
          override def close: Try[Unit] = throw new UnsupportedOperationException()
        }, immediateScheduler, new DirectoryService {
          override def createFolder(folderPath: Path): Try[Unit] = Try(())
        }, instant("2021-03-19T19:00:00Z")).execute(None, 1, 10.millisecond)

        result shouldBe a[Failure[_]]
        result.failed.get.getMessage shouldBe "gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model"
      }

      "when cannot create session folder then fail fast and does not even try to connect to camera" in {
        val camera = new GCamera {
          override def initialize: Try[Unit] = throw new UnsupportedOperationException()
          override def newConfiguration: Try[GConfiguration] = throw new UnsupportedOperationException()
          override def captureImage: Try[GFile] = throw new UnsupportedOperationException()
          override def close: Try[Unit] = throw new UnsupportedOperationException()
        }
        val result = new EttlApp(AppConfiguration(Paths.get("/"), "CR2"), camera, immediateScheduler, new DirectoryService {
          override def createFolder(folderPath: Path): Try[Unit] = Failure(new Exception("cannot create folder, sorry bro"))
        }, instant("2021-03-19T19:00:00Z"))
          .execute(Some(SettingsCameraModel(Some(1d / 100d), Some(400), Some(2.8))),2, 10.millisecond)

        result shouldBe a[Failure[_]]
        result.failed.get.getMessage shouldBe "cannot create folder, sorry bro"
      }

      "when capture settings are invalid and image cannot be taken" ignore {

      }
    }
  }
}
