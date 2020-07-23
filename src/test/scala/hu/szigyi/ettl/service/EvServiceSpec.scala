package hu.szigyi.ettl.service

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EvServiceSpec extends AnyFunSuite with Matchers {

  test("can calculate baseline ev100=0") {
    EvService.ev(1d, 100, 1d) shouldBe 0d
  }

  test("can calculate different base iso like ev400") {
    EvService.ev(1d, 400, 1d) shouldBe 2d
  }
}
