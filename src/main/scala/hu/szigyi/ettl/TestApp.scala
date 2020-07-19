package hu.szigyi.ettl

import java.time.Clock
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.InfluxDbClient
import hu.szigyi.ettl.service.TimelapseService
import hu.szigyi.ettl.util.ShellKill
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.{AgentProduct, `User-Agent`}
import reflux.InfluxClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object TestApp extends IOApp with StrictLogging {
  private val threadPool = Executors.newFixedThreadPool(1)
  private val backgroundExecutionContext = ExecutionContext.fromExecutor(threadPool)

  override def run(args: List[String]): IO[ExitCode] = {

    BlazeClientBuilder[IO](backgroundExecutionContext)
      .withConnectTimeout(5.seconds)
      .withMaxTotalConnections(40)
      .withMaxConnectionsPerRequestKey(_ => 4)
      .withUserAgent(`User-Agent`(AgentProduct("Mozilla", Some("5.0"))))
      .resource.use { client =>
      val rateOfBgProcess = 1.second
      val clock = Clock.systemUTC()
      val influx = new InfluxClient[IO](client, Uri.unsafeFromString("http://localhost:8086"))
      val influxDbClient = InfluxDbClient.apply(influx)
      val shellKill = new ShellKill()
      val timeLapseService = new TimelapseService(shellKill, influxDbClient, rateOfBgProcess, clock)
      val httpJob = new HttpJob(rateOfBgProcess, timeLapseService)
      val trigger = fs2.Stream.eval(timeLapseService.storeTestTimelapseTask)
      trigger.merge(httpJob.run).compile.drain
    }.as(ExitCode.Success)
  }
}
