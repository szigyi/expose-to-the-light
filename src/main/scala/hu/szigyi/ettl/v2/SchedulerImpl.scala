package hu.szigyi.ettl.v2

import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v2.tool.Timing.time

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.Try

trait Scheduler {
  def scheduleOne[T](lastCaptureTime: Instant, interval: Duration, task: => T): T
  def schedule[T](numberOfTasks: Int, interval: Duration, task: => Try[T]): Try[Seq[T]]
}

class SchedulerImpl(clock: Clock, awakingFrequency: Duration) extends Scheduler with StrictLogging {

  override def scheduleOne[T](lastCaptureTime: Instant, interval: Duration, capture: => T): T = {
    val now = Instant.now(clock)
    val elapsed = Duration(now.toEpochMilli - lastCaptureTime.toEpochMilli, TimeUnit.MILLISECONDS)
    logger.trace(s"last: $lastCaptureTime")
    logger.trace(s"now : $now")
    logger.trace(s"elapsed: ${elapsed.toMillis}")
    logger.trace(s"interva: ${interval.toMillis}")
    if (elapsed >= interval) {
      capture // Can it cause problem? There is no explicit apply!
    } else {
      logger.trace("feeling sleepy...")
      Thread.sleep(awakingFrequency.toMillis)
      scheduleOne(lastCaptureTime, interval, capture)
    }
  }

  override def schedule[T](numberOfTasks: Int, interval: Duration, task: => Try[T]): Try[Seq[T]] = {
    val start = Instant.now(clock)
    logger.trace(s"Schedule starts: $start")
    (-1 until (numberOfTasks - 1)).toList.traverse {
      case -1 =>
        time("Schedule took", task)
      case index =>
        val lastTimeStarted = start.plusMillis(interval.toMillis * index)
        time("Schedule took", scheduleOne(lastTimeStarted, interval, task))
    }
  }
}
