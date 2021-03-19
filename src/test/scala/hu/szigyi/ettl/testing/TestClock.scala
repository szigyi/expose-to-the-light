package hu.szigyi.ettl.testing

import com.typesafe.scalalogging.StrictLogging

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.duration.Duration

object TestClock {

  /**
    * Every time the time is asked, the clock ticks, moves ahead by @tickDuration
    */
  class TickingClock(start: Instant, tickDuration: Duration, zone: ZoneId = ZoneId.of("UTC")) extends Clock {
    private var now: Instant = start

    override def getZone: ZoneId = zone

    override def withZone(withZone: ZoneId): Clock =
      new TickingClock(now, tickDuration, withZone)

    override def instant(): Instant = {
      now = now.plusNanos(tickDuration.toNanos)
      now
    }
  }

  /**
    * acceleration is the duration that will be multiplied the elapsed milliseconds
    * which means if the acceleration is 1 second then
    * if 1 millisecond is elapsed then it will be seen as 1 second
    */
  class AcceleratedClock(start: Instant, createClock: Instant => Clock, acceleration: Duration, zone: ZoneId = ZoneId.of("UTC")) extends Clock with StrictLogging {
    private var previousRead: Instant = start
    private var clock: Clock          = createClock(start)

    override def getZone: ZoneId = zone

    override def withZone(withZone: ZoneId): Clock =
      new AcceleratedClock(previousRead, createClock, acceleration, withZone)

    override def instant(): Instant = {
      val now                      = clock.instant()
      val elapsedMillisInNormal    = now.toEpochMilli - previousRead.toEpochMilli
      val elapsedMillisAccelerated = elapsedMillisInNormal * acceleration.toMillis
      logger.trace(s"previousRead: $previousRead")
      logger.trace(s"now:          $now")
      logger.trace(s"elapsedMillisInNormal:    $elapsedMillisInNormal")
      logger.trace(s"elapsedMillisAccelerated: $elapsedMillisAccelerated")
      previousRead = previousRead.plusMillis(elapsedMillisAccelerated)
      clock = createClock(previousRead)
      previousRead
    }
  }

  object AcceleratedClock {
    def apply(start: String, tickDuration: Duration, acceleration: Duration): AcceleratedClock =
      new AcceleratedClock(Instant.parse(start), start => new TickingClock(start, tickDuration), acceleration)
  }

}
