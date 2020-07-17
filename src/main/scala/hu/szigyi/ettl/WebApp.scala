package hu.szigyi.ettl

import java.util.concurrent.Executors

import cats.syntax.functor._
import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.{AgentProduct, `User-Agent`}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object WebApp extends IOApp with StrictLogging {

  private val port = sys.env.getOrElse("http_port", "8230").toInt
  private val env = sys.env.getOrElse("ENV", "local")

  private val threadPool = Executors.newFixedThreadPool(1)
  private val backgroundExecutionContext = ExecutionContext.fromExecutor(threadPool)

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeClientBuilder[IO](backgroundExecutionContext)
      .withConnectTimeout(5.seconds)
      .withMaxTotalConnections(40)
      .withMaxConnectionsPerRequestKey(_ => 4)
      .withUserAgent(`User-Agent`(AgentProduct("Mozilla", Some("5.0"))))
      .resource.use { client =>

      val ioc = new InverseOfControl(env, port, client)
      val apiStream = ioc.httpApi.run
      val jobStream = ioc.httpJob.run
      apiStream.merge(jobStream).compile.drain
    }
      .handleErrorWith(logErrorAsAFinalFrontier)
      .map(_ => threadPool.shutdown())
      .map(_ => finalWords())
      .as(ExitCode.Success)
  }

  private def logErrorAsAFinalFrontier(throwable: Throwable): IO[Unit] = {
    logger.error(s"App is terminating because of ${throwable.getMessage}")
    IO.raiseError(throwable)
  }

  private def finalWords(): Unit =
    logger.info(s"App is terminating as you said so!")

}
