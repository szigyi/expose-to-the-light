package hu.szigyi.ettl.v2

import com.typesafe.scalalogging.StrictLogging

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

trait Scheduler {
  def schedule[T](lastCaptureTime: Instant, interval: Duration, capture: () => T): T
}

class SchedulerImpl(clock: Clock, frequencyToCheck: Duration) extends Scheduler with StrictLogging {

  override def schedule[T](lastCaptureTime: Instant, interval: Duration, capture: () => T): T = {
    val now = Instant.now(clock)
    val elapsed = Duration(now.toEpochMilli - lastCaptureTime.toEpochMilli, TimeUnit.MILLISECONDS)
    logger.debug(s"last: ${lastCaptureTime}")
    logger.debug(s"now : ${now}")

    logger.debug(s"elapsed: ${elapsed.toMillis}")
    logger.debug(s"interva: ${interval.toMillis}")
    if (elapsed >= interval) {
      capture()
    } else {
      logger.debug("feeling sleepy...")
      Thread.sleep(frequencyToCheck.toMillis)
      schedule(lastCaptureTime, interval, capture)
    }
  }
}
