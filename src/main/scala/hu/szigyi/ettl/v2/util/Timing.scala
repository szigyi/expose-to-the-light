package hu.szigyi.ettl.v2.util

import com.typesafe.scalalogging.StrictLogging

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

object Timing extends StrictLogging {

  def time[T](name: String, task: => T): T = {
    val started = System.nanoTime()
    val result = task
    val finished = System.nanoTime()
    val duration = Duration.apply(finished - started, TimeUnit.NANOSECONDS)
    logger.debug(s"$name: ${duration.toMillis}ms | ${duration.toNanos}ns")
    result
  }
}
