package hu.szigyi.ettl.service

import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.util.Timing.time

import java.time.{Clock, Instant, LocalTime, ZoneId}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.Try

trait Scheduler {
  def scheduleOne[T](nextTimeShouldStartAt: Instant, task: => T): T
  def schedule[T](numberOfTasks: Int, interval: Duration, task: Int => Try[T]): Try[Seq[T]]
}

class SchedulerImpl(clock: Clock, awakingFrequency: Duration) extends Scheduler with StrictLogging {

  override def scheduleOne[T](nextTimeShouldStartAt: Instant, capture: => T): T = {
    val now = Instant.now(clock)
    val elapsed = Duration(nextTimeShouldStartAt.toEpochMilli - now.toEpochMilli, TimeUnit.MILLISECONDS)
    logger.trace(s"Waiting $elapsed -> Next: ${instantToLocalTime(nextTimeShouldStartAt)} - Now: ${instantToLocalTime(now)}")
    if (elapsed.toMillis <= 0) {
      capture
    } else {
      logger.trace("feeling sleepy...")
      Thread.sleep(awakingFrequency.toMillis)
      scheduleOne(nextTimeShouldStartAt, capture)
    }
  }

  override def schedule[T](numberOfTasks: Int, interval: Duration, task: Int => Try[T]): Try[Seq[T]] = {
    var start: Instant = null
    logger.trace(s"Scheduling $numberOfTasks tasks")
    (0 until numberOfTasks).toList.traverse {
      case 0 =>
        logger.trace(s"Preparing for the First task: ${Instant.now(clock)}")
        time(s"[1/$numberOfTasks] First Schedule took", {
          start = Instant.now(clock)
          logger.info(s"Schedule starts: $start")
          task(1)
        })
      case index =>
        val nextTimeShouldStartAt = start.plusMillis(interval.toMillis * index)
        val imageCount = index + 1
        time(s"[$imageCount/$numberOfTasks] Schedule took", scheduleOne(nextTimeShouldStartAt, task(imageCount)))
    }
  }

  private def instantToLocalTime(i: Instant): LocalTime =
    LocalTime.from(i.atZone(ZoneId.systemDefault()))
}
