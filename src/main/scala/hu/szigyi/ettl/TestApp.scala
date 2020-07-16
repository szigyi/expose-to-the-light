package hu.szigyi.ettl

import java.util.concurrent.Executors

import cats.syntax.functor._
import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.service.TimelapseService
import hu.szigyi.ettl.util.ShellKill

import scala.concurrent.{ExecutionContext}

object TestApp extends IOApp with StrictLogging  {

  private val fixedThreadPool = Executors.newFixedThreadPool(1)
  private implicit val backgroundExecutionContext = ExecutionContext.fromExecutorService(fixedThreadPool)

  override def run(args: List[String]): IO[ExitCode] = {
    val shellKill = new ShellKill()
    val timeLapseService = new TimelapseService(shellKill)
    timeLapseService.test
        .map {
          case Left(error) => logger.error(
            s"""
              |${error.result}
              |${error.msg}
              |${error.suggestion}""".stripMargin)
          case Right(value) => logger.info(value)
        }
      .map(_ => {
        backgroundExecutionContext.shutdown()
      })
      .as(ExitCode.Success)
  }
}
