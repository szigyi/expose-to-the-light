package hu.szigyi.ettl.v1

import cats.effect.{Blocker, ContextShift, IO, Timer}
import hu.szigyi.ettl.v1.api._
import hu.szigyi.ettl.v1.influx.InfluxDbClient
import hu.szigyi.ettl.v1.service.{CapturedImageService, TimelapseService}
import org.http4s.Uri
import org.http4s.client.Client
import reflux.InfluxClient

import java.time.Clock
import scala.concurrent.duration._

class InverseOfControl(env: String, port: Int, client: Client[IO])(implicit timer: Timer[IO], contextShift: ContextShift[IO]) {

  val blocker: Blocker = Blocker[IO].allocated.unsafeRunSync()._1

  val clock = Clock.systemUTC()

  val influx = new InfluxClient[IO](client, Uri.unsafeFromString("http://localhost:8086"))
  val influxDbClient = InfluxDbClient.apply(influx)

  val rateOfBgProcess = 1.second
  val capturedImageService = new CapturedImageService()
  val timeLapseService = new TimelapseService(influxDbClient, capturedImageService, rateOfBgProcess, clock)

  val staticApi: StaticApi = new StaticApi(blocker)
  val healthApi: HealthApi = new HealthApi(env)
  val keyFrameApi: KeyFrameApi = new KeyFrameApi(influxDbClient)
  val timeLapseAPi = new TimelapseApi(timeLapseService)
  val lastCapturedApi = new LastCapturedApi(capturedImageService)

  val httpApi = new HttpApi(env, port, staticApi, healthApi, keyFrameApi, timeLapseAPi, lastCapturedApi)
  val httpJob = new HttpJob(rateOfBgProcess, timeLapseService)
}
