package hu.szigyi.ettl.v2
import java.time.{Clock, Instant}
import scala.concurrent.duration.Duration

object SchedulerFixture {

  def immediateScheduler: Scheduler = new Scheduler {
    override def schedule[T](lastCaptureTime: Instant, interval: Duration, capture: () => T): T =
      capture()
  }
}
