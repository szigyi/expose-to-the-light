package hu.szigyi.ettl

import cats.effect.{Blocker, ContextShift, IO, Timer}
import hu.szigyi.ettl.api.{HealthApi, SettingsApi, StaticApi, TimelapseApi}
import hu.szigyi.ettl.service.{CameraService, TimelapseService}
import hu.szigyi.ettl.util.ShellKill

import scala.concurrent.ExecutionContext

class InverseOfControl(env: String)(implicit backgroundProcesses: ExecutionContext, contextShift: ContextShift[IO]) {

  val blocker: Blocker = Blocker[IO].allocated.unsafeRunSync()._1

  val shellKill = new ShellKill()
  val cameraService = new CameraService(shellKill)
  val timeLapseService = new TimelapseService(cameraService)

  val staticApi: StaticApi = new StaticApi(blocker)
  val healthApi: HealthApi = new HealthApi(env)
  val settingsApi: SettingsApi = new SettingsApi()
  val timeLapseAPi = new TimelapseApi(timeLapseService)
}
