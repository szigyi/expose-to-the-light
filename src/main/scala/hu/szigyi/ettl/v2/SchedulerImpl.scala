package hu.szigyi.ettl.v2

import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v2.tool.Timing.time

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.Try

trait Scheduler {
  def schedule[T](lastCaptureTime: Instant, interval: Duration, task: => T): T
  def schedule[T](numberOfTasks: Int, interval: Duration, task: => Try[T]): Try[Seq[T]]
}

class SchedulerImpl(clock: Clock, frequencyToCheck: Duration) extends Scheduler with StrictLogging {

  override def schedule[T](lastCaptureTime: Instant, interval: Duration, capture: => T): T = {
    val now = Instant.now(clock)
    val elapsed = Duration(now.toEpochMilli - lastCaptureTime.toEpochMilli, TimeUnit.MILLISECONDS)
    logger.trace(s"last: ${lastCaptureTime}")
    logger.trace(s"now : ${now}")
    logger.trace(s"elapsed: ${elapsed.toMillis}")
    logger.trace(s"interva: ${interval.toMillis}")
    if (elapsed >= interval) {
      capture // Can it cause problem? There is no explicit apply!
    } else {
      logger.trace("feeling sleepy...")
      Thread.sleep(frequencyToCheck.toMillis)
      schedule(lastCaptureTime, interval, capture)
    }
  }

  override def schedule[T](numberOfTasks: Int, interval: Duration, task: => Try[T]): Try[Seq[T]] = {
    val start = Instant.now(clock)
    (0 until numberOfTasks).toList.traverse { index =>
      val lastTimeStarted = start.plusMillis(interval.toMillis * index)
      time("Schedule took", schedule(lastTimeStarted, interval, task))
    }
  }
}