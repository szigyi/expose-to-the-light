package hu.szigyi.ettl.v1.tools

import java.time.Clock
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import cats.syntax.apply._
import hu.szigyi.ettl.v1.influx.InfluxDbClient
import hu.szigyi.ettl.v1.influx.InfluxDomain.KeyFrameDomain
import KeyFrameDomain._
import hu.szigyi.ettl.v1.service.KeyFrameService.CurvedSetting
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
          *> influxDbClient.writeKeyFrame(playingWithEv(clock, "playful-curvature"))
      )
      storeTask.compile.drain
    }
      .map(_ => threadPool.shutdown())
      .as(ExitCode.Success)
  }

  private def static(clock: Clock, id: String): Seq[KeyFrameDomain] =
    sparseInTimeKeyFrames(clock, Seq(
      KeyFrameDomain(clock.instant(), id, 0.second, 15d, "15", 1600, 2.8d),
      KeyFrameDomain(clock.instant(), id, 60.minutes, 15d, "15", 1600, 2.8d)
    ))

  private def curvature(clock: Clock, id: String): Seq[KeyFrameDomain] =
    sparseInTimeKeyFrames(clock, Seq(
      CurvedSetting(20.0.minutes, 15d, 1600, 2.8),
      CurvedSetting(15.0.minutes, 13, 1600, 2.8),
      CurvedSetting(15.0.minutes, 10, 1600, 2.8),
      CurvedSetting(10.0.minutes, 8d, 800, 2.8),
      CurvedSetting(8.0.minutes, 6d, 800, 2.8),
      CurvedSetting(5.0.minutes, 5d, 800, 2.8),
      CurvedSetting(4.0.minutes, 3.2d, 800, 2.8),
        CurvedSetting(4.0.minutes, 2.5d, 800, 2.8),
        CurvedSetting(4.0.minutes, 2d, 800, 2.8),
        CurvedSetting(4.0.minutes,  1.6d, 800, 2.8),
      CurvedSetting(4.0.minutes, 1.3d, 400, 2.8),
      CurvedSetting(3.0.minutes, 1d, 400, 2.8),
      CurvedSetting(3.0.minutes, 0.8d, 400, 2.8),
      CurvedSetting(3.0.minutes, 0.6d, 400, 2.8),
      CurvedSetting(3.0.minutes, 0.5d, 400, 2.8),
        CurvedSetting(3.0.minutes,  0.4d, 400, 2.8),
        CurvedSetting(3.0.minutes,  0.3d, 400, 2.8),
        CurvedSetting(3.0.minutes,  1d/4d, 400, 2.8),
        CurvedSetting(3.0.minutes,  1d/5d, 400, 2.8),
      CurvedSetting(3.0.minutes, 1d / 6d, 200, 2.8),
      CurvedSetting(3.0.minutes, 1d / 8d, 200, 2.8),
      CurvedSetting(3.0.minutes, 1d / 10d, 200, 2.8),
        CurvedSetting(3.0.minutes,  1d/13d, 200, 2.8),
        CurvedSetting(3.0.minutes,  1d/15d, 200, 2.8),
        CurvedSetting(3.0.minutes,  1d/20d, 200, 2.8),
        CurvedSetting(3.0.minutes,  1d/25d, 200, 2.8),
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
    .map(c => KeyFrameDomain(clock.instant(), id, c.duration, c.shutterSpeed, c.shutterSpeedString, c.iso, c.aperture)))

  private def playingWithEv(clock: Clock, id: String): Seq[KeyFrameDomain] =
    sparseInTimeKeyFrames(clock, Seq(
      CurvedSetting(30.seconds, 8.0,8000,18.0),
      CurvedSetting(30.seconds, 0.5,8000,4.5),
      CurvedSetting(30.seconds, 0.0125,250,4.5),
      CurvedSetting(30.seconds, 0.4,1600,9.0),
      CurvedSetting(30.seconds, 15.0,10000,22.0),
      CurvedSetting(30.seconds, 0.4,400,18.0),
      CurvedSetting(30.seconds, 0.05,250,9.0),
      CurvedSetting(30.seconds, 0.3,320,18.0),
      CurvedSetting(30.seconds, 2.5,2500,18.0),
      CurvedSetting(30.seconds, 0.8,3200,9.0),
      CurvedSetting(30.seconds, 2.5,10000,9.0),
      CurvedSetting(30.seconds, 1.6,6400,9.0),
      CurvedSetting(30.seconds, 0.3,250,22.0),
      CurvedSetting(30.seconds, 0.2,250,18.0),
      CurvedSetting(30.seconds, 0.25,4000,4.5),
      CurvedSetting(30.seconds, 0.25,1000,9.0),
      CurvedSetting(30.seconds, 2.0,2000,18.0),
      CurvedSetting(30.seconds, 0.6,640,18.0),
      CurvedSetting(30.seconds, 0.8,800,18.0),
      CurvedSetting(30.seconds, 0.1,400,9.0),
      CurvedSetting(30.seconds, 0.025,400,4.5),
      CurvedSetting(30.seconds, 0.5,3200,7.1),
      CurvedSetting(30.seconds, 3.2,12800,9.0),
      CurvedSetting(30.seconds, 0.05,200,9.0),
      CurvedSetting(30.seconds, 0.025,125,9.0),
      CurvedSetting(30.seconds, 0.00625,125,4.5),
      CurvedSetting(30.seconds, 3.2,3200,18.0),
      CurvedSetting(30.seconds, 1.0,1000,18.0),
      CurvedSetting(30.seconds, 0.1,1600,4.5),
      CurvedSetting(30.seconds, 0.5,2000,9.0),
      CurvedSetting(30.seconds, 0.5,500,18.0),
      CurvedSetting(30.seconds, 0.3,1250,9.0),
      CurvedSetting(30.seconds, 0.2,800,9.0),
      CurvedSetting(30.seconds, 0.05,800,4.5),
      CurvedSetting(30.seconds, 0.125,2000,4.5),
      CurvedSetting(30.seconds, 0.0125,200,4.5),
      CurvedSetting(30.seconds, 0.2,3200,4.5),
      CurvedSetting(30.seconds, 0.6,1600,11.0),
      CurvedSetting(30.seconds, 2.0,12800,7.1),
      CurvedSetting(30.seconds, 0.125,800,7.1),
      CurvedSetting(30.seconds, 0.025,100,9.0),
      CurvedSetting(30.seconds, 2.0,8000,9.0),
      CurvedSetting(30.seconds, 0.25,1600,7.1),
      CurvedSetting(30.seconds, 0.00625,100,4.5),
      CurvedSetting(30.seconds, 5.0,5000,18.0),
      CurvedSetting(30.seconds, 1.0,4000,9.0),
      CurvedSetting(30.seconds, 0.025,160,9.0),
      CurvedSetting(30.seconds, 0.1,100,18.0),
      CurvedSetting(30.seconds, 6.0,4000,22.0),
      CurvedSetting(30.seconds, 1.0,6400,7.1),
      CurvedSetting(30.seconds, 10.0,10000,18.0),
      CurvedSetting(30.seconds, 0.3,200,22.0),
      CurvedSetting(30.seconds, 1.6,1600,18.0),
      CurvedSetting(30.seconds, 0.3,800,11.0),
      CurvedSetting(30.seconds, 0.125,500,9.0),
      CurvedSetting(30.seconds, 0.1,160,18.0),
      CurvedSetting(30.seconds, 0.1,125,18.0),
      CurvedSetting(30.seconds, 0.00625,160,4.5),
      CurvedSetting(30.seconds, 0.2,200,18.0),
      CurvedSetting(30.seconds, 0.6,400,22.0),
      CurvedSetting(30.seconds, 0.4,6400,4.5),
      CurvedSetting(30.seconds, 0.8,12800,4.5)
    )
      .map(c => KeyFrameDomain(clock.instant(), id, c.duration, c.shutterSpeed, c.shutterSpeedString, c.iso, c.aperture)))

  private def sparseInTimeKeyFrames(clock: Clock, keyFrames: Seq[KeyFrameDomain]): Seq[KeyFrameDomain] = {
    val now = clock.instant()
    val idx = 0 until keyFrames.size
    idx.zip(keyFrames).map {
      case (i, k) =>
        k.copy(timestamp = now.plusSeconds(i))
    }
  }
}
