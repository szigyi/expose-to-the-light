package hu.szigyi.ettl

import java.time.Clock

import cats.effect.{Blocker, ContextShift, IO, Timer}
import hu.szigyi.ettl.api.{HealthApi, SettingsApi, StaticApi, TimelapseApi}
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.service.TimelapseService
import hu.szigyi.ettl.util.ShellKill
import org.http4s.Uri
import org.http4s.client.Client
import reflux.InfluxClient

import scala.concurrent.duration._

class InverseOfControl(env: String, port: Int, client: Client[IO])(implicit timer: Timer[IO], contextShift: ContextShift[IO]) {

  val blocker: Blocker = Blocker[IO].allocated.unsafeRunSync()._1

  val clock = Clock.systemUTC()

  val influx = new InfluxClient[IO](client, Uri.unsafeFromString("http://localhost:8086"))
  val influxDbClient = InfluxDbClient.apply(influx)

  val rateOfBgProcess = 1.second
  val shellKill = new ShellKill()
  val timeLapseService = new TimelapseService(shellKill, influxDbClient, rateOfBgProcess, clock)

  val staticApi: StaticApi = new StaticApi(blocker)
  val healthApi: HealthApi = new HealthApi(env)
  val settingsApi: SettingsApi = new SettingsApi(influxDbClient)
  val timeLapseAPi = new TimelapseApi(timeLapseService)

  val httpApi = new HttpApi(env, port, staticApi, healthApi, settingsApi, timeLapseAPi)
  val httpJob = new HttpJob(rateOfBgProcess, timeLapseService)
}
