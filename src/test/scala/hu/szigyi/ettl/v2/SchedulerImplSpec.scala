package hu.szigyi.ettl.v2

import hu.szigyi.ettl.v2.TestClock.AcceleratedClock
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.time.Instant.{parse => instant}
import scala.concurrent.duration._

class SchedulerImplSpec extends AnyFreeSpec with Matchers {

  "execute task with perfect timing" in {
    val realtimeTick = 1.millisecond
    val acceleratedTick = 100.millisecond
    val clock = AcceleratedClock("2021-02-19T13:00:00Z", realtimeTick, acceleratedTick)
    val scheduler = new SchedulerImpl(clock)
    val interval = 1.seconds
    val capture: () => Instant = () => clock.instant()

    val result = scheduler.schedule[Instant](instant("2021-02-19T13:00:00Z"), interval, capture)

    result.toString shouldBe "2021-02-19T13:00:01.100Z"
  }

}
