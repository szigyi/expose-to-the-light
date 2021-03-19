package hu.szigyi.ettl.util

import hu.szigyi.ettl.model.ShutterSpeedMap
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ShutterSpeedMapSpec extends AnyFunSuite with Matchers {

  test("can get fraction form of shutter speed") {
    ShutterSpeedMap.toShutterSpeed(0.000125) shouldBe Some("1/8000")
  }
}
