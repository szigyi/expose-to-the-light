package hu.szigyi.ettl.testing

import hu.szigyi.ettl.testing.TestClock.{AcceleratedClock, TickingClock}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant.{parse => instant}
import scala.concurrent.duration._

class TestClockSpec extends AnyFreeSpec with Matchers {
  "1 second clock time in 2 millis real time" in {
    val clock = new AcceleratedClock(instant("2021-02-19T13:00:00.000Z"),
      s => new TickingClock(s, tickDuration = 2.millisecond), acceleration = 1.second)
    clock.instant() shouldBe instant("2021-02-19T13:00:02.000Z")
    clock.instant() shouldBe instant("2021-02-19T13:00:04.000Z")
    clock.instant() shouldBe instant("2021-02-19T13:00:06.000Z")
  }

  "123 millis clock time in 1 millis real time" in {
    val clock = AcceleratedClock("2021-02-19T13:00:00.000Z", tickDuration = 1.millisecond, acceleration = 123.milliseconds)
    clock.instant() shouldBe instant("2021-02-19T13:00:00.123Z")
    clock.instant() shouldBe instant("2021-02-19T13:00:00.246Z")
    clock.instant() shouldBe instant("2021-02-19T13:00:00.369Z")
  }

  "1 millis clock time in 1 second real time" in {
    val clock = AcceleratedClock("2021-02-19T13:00:00.000Z", tickDuration = 1.second, acceleration = 1.millis)
    clock.instant() shouldBe instant("2021-02-19T13:00:01.00Z")
    clock.instant() shouldBe instant("2021-02-19T13:00:02.00Z")
    clock.instant() shouldBe instant("2021-02-19T13:00:03.00Z")
  }
}