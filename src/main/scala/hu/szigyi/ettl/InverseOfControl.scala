package hu.szigyi.ettl

import cats.effect.{Blocker, ContextShift, IO, Timer}
import hu.szigyi.ettl.api.{HealthApi, SettingsApi, StaticApi, TimelapseApi}
import hu.szigyi.ettl.service.{CameraService, TimelapseService}

import scala.concurrent.ExecutionContext

class InverseOfControl(env: String)(implicit backgroundProcesses: ExecutionContext, contextShift: ContextShift[IO]) {

  val blocker: Blocker = Blocker[IO].allocated.unsafeRunSync()._1

  val cameraService = new CameraService()
  val timeLapseService = new TimelapseService(cameraService)

  val staticApi: StaticApi = new StaticApi(blocker)
  val healthApi: HealthApi = new HealthApi(env)
  val settingsApi: SettingsApi = new SettingsApi()
  val timeLapseAPi = new TimelapseApi(timeLapseService)
}
