package hu.szigyi.ettl

import cats.effect.{Blocker, ContextShift, IO}
import hu.szigyi.ettl.api.{CurvatureApi, HealthApi, StaticApi}

class InverseOfControl(env: String)(implicit contextShift: ContextShift[IO]) {

  val blocker = Blocker[IO].allocated.unsafeRunSync()._1

  val staticApi = new StaticApi(blocker)
  val healthApi = new HealthApi(env)
  val curvatureApi = new CurvatureApi()
}
