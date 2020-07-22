package hu.szigyi.ettl.tools

import java.time.Clock
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import cats.syntax.apply._
import hu.szigyi.ettl.client.influx.InfluxDbClient
import hu.szigyi.ettl.client.influx.InfluxDomain.KeyFrame
import KeyFrame._
import hu.szigyi.ettl.service.Curvature.CurvedSetting
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.{AgentProduct, `User-Agent`}
import reflux.InfluxClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object StoreKeyFramesApp extends IOApp {
  private val threadPool = Executors.newFixedThreadPool(1)
  private val backgroundExecutionContext = ExecutionContext.fromExecutor(threadPool)

  override def run(args: List[String]): IO[ExitCode] = {

    BlazeClientBuilder[IO](backgroundExecutionContext)
      .withConnectTimeout(5.seconds)
      .withMaxTotalConnections(40)
      .withMaxConnectionsPerRequestKey(_ => 4)
      .withUserAgent(`User-Agent`(AgentProduct("Mozilla", Some("5.0"))))
      .resource.use { client =>
      val clock = Clock.systemUTC()
      val influx = new InfluxClient[IO](client, Uri.unsafeFromString("http://localhost:8086"))
      val influxDbClient = InfluxDbClient.apply(influx)

      val storeTask: fs2.Stream[IO, Unit] = fs2.Stream.eval(
        influxDbClient.writeKeyFrame(static(clock, "static-curvature"))
          *> influxDbClient.writeKeyFrame(curvature(clock, "sunset-curvature"))
      )
      storeTask.compile.drain
    }
      .map(_ => threadPool.shutdown())
      .as(ExitCode.Success)
  }

  private def static(clock: Clock, id: String): Seq[KeyFrame] =
    sparseInTimeKeyFrames(clock, Seq(
      KeyFrame(clock.instant(), id, 0.second, 15d, "15", 1600, 2.8d),
      KeyFrame(clock.instant(), id, 60.minutes, 15d, "15", 1600, 2.8d)
    ))

  private def curvature(clock: Clock, id: String): Seq[KeyFrame] =
    sparseInTimeKeyFrames(clock, Seq(
      CurvedSetting(20.0.minutes, 15d, 1600, 2.8),
  //    CurvedSetting(15.0.minutes, 13, 1600, 2.8),
  //    CurvedSetting(15.0.minutes, 10, 1600, 2.8),
      CurvedSetting(10.0.minutes, 8d, 800, 2.8),
      CurvedSetting(8.0.minutes, 6d, 800, 2.8),
      CurvedSetting(5.0.minutes, 5d, 800, 2.8),
      CurvedSetting(4.0.minutes, 3.2d, 800, 2.8),
      //  CurvedSetting(4.0.minutes, 2.5d, 800, 2.8),
      //  CurvedSetting(4.0.minutes, 2d, 800, 2.8),
      //  CurvedSetting(4.0.minutes,  1.6d, 800, 2.8),
      CurvedSetting(4.0.minutes, 1.3d, 400, 2.8),
      CurvedSetting(3.0.minutes, 1d, 400, 2.8),
      CurvedSetting(3.0.minutes, 0.8d, 400, 2.8),
      CurvedSetting(3.0.minutes, 0.6d, 400, 2.8),
      CurvedSetting(3.0.minutes, 0.5d, 400, 2.8),
      //  CurvedSetting(3.0.minutes,  0.4d, 400, 2.8),
      //  CurvedSetting(3.0.minutes,  0.3d, 400, 2.8),
      //  CurvedSetting(3.0.minutes,  1d/4d, 400, 2.8),
      //  CurvedSetting(3.0.minutes,  1d/5d, 400, 2.8),
      CurvedSetting(3.0.minutes, 1d / 6d, 200, 2.8),
      CurvedSetting(3.0.minutes, 1d / 8d, 200, 2.8),
      CurvedSetting(3.0.minutes, 1d / 10d, 200, 2.8),
      //  CurvedSetting(3.0.minutes,  1d/13d, 200, 2.8),
      //  CurvedSetting(3.0.minutes,  1d/15d, 200, 2.8),
      //  CurvedSetting(3.0.minutes,  1d/20d, 200, 2.8),
      //  CurvedSetting(3.0.minutes,  1d/25d, 200, 2.8),
      CurvedSetting(3.0.minutes, 1d / 30d, 100, 2.8),
      CurvedSetting(3.0.minutes, 1d / 40d, 100, 2.8),
      CurvedSetting(3.0.minutes, 1d / 50d, 100, 2.8),
      CurvedSetting(3.0.minutes, 1d / 60d, 100, 2.8),
      CurvedSetting(4.0.minutes, 1d / 80d, 100, 2.8),
      CurvedSetting(4.0.minutes, 1d / 100d, 100, 2.8),
      CurvedSetting(4.0.minutes, 1d / 125d, 100, 2.8),
      CurvedSetting(4.0.minutes, 1d / 160d, 100, 2.8),
      CurvedSetting(4.0.minutes, 1d / 200d, 100, 2.8),
      CurvedSetting(5.0.minutes, 1d / 250d, 100, 2.8),
      CurvedSetting(5.0.minutes, 1d / 320d, 100, 2.8),
      CurvedSetting(5.0.minutes, 1d / 400d, 100, 2.8),
      CurvedSetting(5.0.minutes, 1d / 500d, 100, 2.8),
      CurvedSetting(5.0.minutes, 1d / 640d, 100, 2.8),
      CurvedSetting(10.0.minutes, 1d / 800d, 100, 2.8),
      CurvedSetting(10.0.minutes, 1d / 1000d, 100, 2.8),
      CurvedSetting(10.0.minutes, 1d / 1250d, 100, 2.8),
      CurvedSetting(10.0.minutes, 1d / 1600d, 100, 2.8),
      CurvedSetting(10.0.minutes, 1d / 2000d, 100, 2.8),
      CurvedSetting(15.0.minutes, 1d / 2500d, 100, 2.8),
      CurvedSetting(15.0.minutes, 1d / 3200d, 100, 2.8),
      CurvedSetting(15.0.minutes, 1d / 4000d, 100, 2.8),
      CurvedSetting(15.0.minutes, 1d / 5000d, 100, 2.8),
      CurvedSetting(20.0.minutes, 1d / 6400d, 100, 2.8),
      CurvedSetting(20.0.minutes, 1d / 8000d, 100, 2.8)
    )
    .map(c => KeyFrame(clock.instant(), id, c.duration, c.shutterSpeed, c.shutterSpeedString, c.iso, c.aperture)))

  private def sparseInTimeKeyFrames(clock: Clock, keyFrames: Seq[KeyFrame]): Seq[KeyFrame] = {
    val now = clock.instant()
    val idx = 0 until keyFrames.size
    idx.zip(keyFrames).map {
      case (i, k) =>
        k.copy(timestamp = now.plusSeconds(i))
    }
  }
}
