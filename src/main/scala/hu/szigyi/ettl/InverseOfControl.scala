package hu.szigyi.ettl

import cats.effect.{Blocker, ContextShift, IO}
import hu.szigyi.ettl.api.{SettingsApi, HealthApi, StaticApi}

class InverseOfControl(env: String)(implicit contextShift: ContextShift[IO]) {

  val blocker: Blocker = Blocker[IO].allocated.unsafeRunSync()._1

  val staticApi: StaticApi = new StaticApi(blocker)
  val healthApi: HealthApi = new HealthApi(env)
  val settingsApi: SettingsApi = new SettingsApi()
}
