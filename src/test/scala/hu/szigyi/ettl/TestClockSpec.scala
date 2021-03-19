package hu.szigyi.ettl

import TestClock.{AcceleratedClock, TickingClock}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant.{parse => instant}
import scala.concurrent.duration._

class TestClockSpec extends AnyFreeSpec with Matchers {
  "accelerate 1 millis to 1 second" in {
    val start = instant("2021-02-19T13:00:00.000Z")
    val acceleratedClock = new AcceleratedClock(start, s => new TickingClock(s, 1.millisecond), 1.second)
    acceleratedClock.instant() shouldBe instant("2021-02-19T13:00:01.000Z")
  }

  "accelerate 1 millis to 123 millis" in {
    val acceleratedClock = AcceleratedClock("2021-02-19T13:00:00.000Z", 1.millisecond, 123.milliseconds)
    acceleratedClock.instant() shouldBe instant("2021-02-19T13:00:00.123Z")
  }
}