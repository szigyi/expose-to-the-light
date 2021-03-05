package hu.szigyi.ettl.v1

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toFunctorOps
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v1.influx.InfluxDbClient
import hu.szigyi.ettl.v1.service.{CapturedImageService, TimelapseService}
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.{AgentProduct, `User-Agent`}
import reflux.InfluxClient

import java.time.Clock
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationDouble, DurationInt}

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
      val rateOfBgProcess = 0.25.seconds
      val clock = Clock.systemUTC()
      val influx = new InfluxClient[IO](client, Uri.unsafeFromString("http://localhost:8086"))
      val influxDbClient = InfluxDbClient.apply(influx)
      val capturedImageService = new CapturedImageService()
      val timeLapseService = new TimelapseService(influxDbClient, capturedImageService, rateOfBgProcess, clock)
      val httpJob = new HttpJob(rateOfBgProcess, timeLapseService)
      val storeTask = fs2.Stream.eval(timeLapseService.storeTestSettings("sunset-curvature"))
      storeTask.merge(httpJob.run).compile.drain
    }
      .map(_ => threadPool.shutdown())
      .as(ExitCode.Success)
  }
}