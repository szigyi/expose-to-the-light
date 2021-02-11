package hu.szigyi.ettl.v2

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Failure

class EttlAppSpec extends AnyFreeSpec with Matchers {

  "runEttl" - {
    "Normal Scenario" - {
      "capture image and returns them" in {

      }
    }

    "Error Scenario" - {
      "when cannot connect to the camera then fast fail and return error" in {
        val result = EttlApp.runEttl(new GCameraImpl())
        result shouldBe a[Failure[_]]
        result.failed.get.getMessage shouldBe "gp_camera_init failed with GP_ERROR_MODEL_NOT_FOUND #-105: Unknown model"
      }
    }
  }
}
