package hu.szigyi.ettl.v2

import cats.implicits._
import java.time.{Clock, Instant}
import scala.concurrent.duration.Duration
import scala.util.Try

object SchedulerFixture {

  def immediateScheduler: Scheduler = new Scheduler {
    override def schedule[T](lastCaptureTime: Instant, interval: Duration, capture: => T): T =
      capture

    override def schedule[T](numberOfTasks: Int, interval: Duration, task: => Try[T]): Try[Seq[T]] =
      (0 until numberOfTasks).toList.traverse { _ => task }
  }
}
