package hu.szigyi.ettl

import hu.szigyi.ettl.service.SchedulerImpl
import TestClock.AcceleratedClock
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.time.Instant.{parse => instant}
import scala.concurrent.duration._
import scala.util.Try

class SchedulerImplSpec extends AnyFreeSpec with Matchers {

  "execute task with perfect timing" in {
    val realtimeTick = 1.millisecond
    val acceleratedTick = 100.millisecond
    val clock = AcceleratedClock("2021-02-19T13:00:00Z", realtimeTick, acceleratedTick)
    val scheduler = new SchedulerImpl(clock, 100.milliseconds)
    val interval = 1.seconds
    def capture: Instant = clock.instant()

    val result = scheduler.scheduleOne[Instant](instant("2021-02-19T13:00:00Z"), interval, capture)

    result.toString shouldBe "2021-02-19T13:00:01.100Z"
  }

  "execute tasks with perfect timings" in {
    val realtimeTick = 1.millisecond
    val acceleratedTick = 100.millisecond
    val clock = AcceleratedClock("2021-02-19T13:00:00Z", realtimeTick, acceleratedTick)
    val scheduler = new SchedulerImpl(clock, 10.milliseconds)
    val interval = 1.seconds
    def capture(i: Int): Try[Instant] = Try(clock.instant())

    val result = scheduler.schedule[Instant](3, interval, capture)

    // +100 millis when starting the schedule as the TickingClock is implemented
    // +100 millis when running the test method, capture to get the instant
    result.get.map(_.toString) should contain theSameElementsAs Seq(
      "2021-02-19T13:00:00.200Z",
      "2021-02-19T13:00:01.200Z",
      "2021-02-19T13:00:02.200Z"
    )
  }

  "execute long/short running tasks with perfect timings" in {
    val realtimeTick = 1.millisecond
    val acceleratedTick = 100.milliseconds
    val clock = AcceleratedClock("2021-02-19T13:00:00Z", realtimeTick, acceleratedTick)
    val scheduler = new SchedulerImpl(clock, 10.milliseconds)
    val interval = 1.seconds
    val bufferInTimeForExecutingTask = 300.milliseconds
    def capture(i: Int): Try[Instant] =
      Try {
        val timesToTickTheClockToImitateSleeping = bufferInTimeForExecutingTask / acceleratedTick
        (0 until timesToTickTheClockToImitateSleeping.toInt).foreach(_ => clock.instant())
        clock.instant()
      }

    val result = scheduler.schedule[Instant](3, interval, capture)

    // +100 millis when starting the schedule as the TickingClock is implemented
    // +100 millis when running the test method, capture to get the instant
    // +300 millis when sleeping, taking long time to finish the task
    result.get.map(_.toString) should contain theSameElementsAs Seq(
      "2021-02-19T13:00:00.500Z",
      "2021-02-19T13:00:01.500Z",
      "2021-02-19T13:00:02.500Z"
    )
  }

  "fails to execute next task in time when task takes longer than the interval but still executes it" in {
    val realtimeTick = 1.millisecond
    val acceleratedTick = 100.milliseconds
    val clock = AcceleratedClock("2021-02-19T13:00:00Z", realtimeTick, acceleratedTick)
    val scheduler = new SchedulerImpl(clock, 10.milliseconds)
    val interval = 1.seconds
    var bufferInTimeForExecutingTask: FiniteDuration = 1000.milliseconds
    def capture(i: Int): Try[Instant] =
      Try {
        val timesToTickTheClockToImitateSleeping = bufferInTimeForExecutingTask / acceleratedTick
        (0 until timesToTickTheClockToImitateSleeping.toInt).foreach(_ => clock.instant())
        bufferInTimeForExecutingTask = bufferInTimeForExecutingTask.div(2)
        clock.instant()
      }

    val result = scheduler.schedule[Instant](3, interval, capture)

    result.get.map(_.toString) should contain theSameElementsAs Seq(
      // +100 millis when starting the schedule as the TickingClock is implemented
      // +100 millis when running the test method, capture to get the instant
      // +1000 millis when sleeping, taking long time to finish the task
      "2021-02-19T13:00:01.200Z",

      // +100 millis when running the test method, capture to get the instant
      // +100 millis when hu.szigyi.ettl.v2.service.SchedulerImpl.scheduleOne gets now
      // +500 millis when sleeping, taking long time to finish the task
      "2021-02-19T13:00:01.900Z",

      // +100 millis when running the test method, capture to get the instant
      // +100 millis when hu.szigyi.ettl.v2.service.SchedulerImpl.scheduleOne gets now
      // +250 millis when sleeping, taking long time to finish the task
      "2021-02-19T13:00:02.400Z", // back to original timing now
    )
  }
}
