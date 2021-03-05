package hu.szigyi.ettl.v1

import cats.effect.{IO, Timer}
import hu.szigyi.ettl.v1.service.TimelapseService

import scala.concurrent.duration.FiniteDuration

class HttpJob(rate: FiniteDuration, tls: TimelapseService)(implicit timer: Timer[IO]) {

  def run: fs2.Stream[IO, Unit] = {
    schedule(rate, tls.executeCurrentTask)
  }

  private def schedule[A](rate: FiniteDuration , task: IO[A]): fs2.Stream[IO, A] =
    fs2.Stream.eval(task) ++ fs2.Stream.repeatEval(task).metered(rate)
}