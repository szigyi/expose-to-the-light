package hu.szigyi.ettl.client.influx

import java.time.Instant

import cats.Functor
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.influx.InfluxDomain.{Captured, TimelapseTask}
import reflux._

class InfluxDbClient[F[_]: Functor](influx: InfluxClient[F]) {

  def getTimelapseTasks(from: Instant, to: Instant): F[Vector[TimelapseTask]] =
    influx.asVector[TimelapseTask](
      s"""
         |SELECT
         |${TimelapseTask.idFieldName},
         |${TimelapseTask.testFieldName},
         |${TimelapseTask.createdFieldName},
         |${TimelapseTask.shutterSpeedFieldName},
         |${TimelapseTask.isoFieldName},
         |${TimelapseTask.apertureFieldName},
         |${TimelapseTask.evFieldName}
         |FROM ${TimelapseTask.measurementName}
         |WHERE time >= '$from' AND time < '$to'""".stripMargin)

  def getCaptured(since: Instant): F[Vector[Captured]] = {
    influx.asVector[Captured](
      s"""
         |SELECT
         |${Captured.idFieldName},
         |${Captured.testFieldName},
         |${Captured.shutterSpeedFieldName},
         |${Captured.isoFieldName},
         |${Captured.apertureFieldName},
         |${Captured.evFieldName},
         |${Captured.errorFieldName},
         |${Captured.suggestionFieldName}
         |FROM ${Captured.measurementName}
         |WHERE time >= '$since'""".stripMargin)
  }

  def writeTimelapseTasks(tlt: Seq[TimelapseTask]): F[Unit] = influx.write(tlt)
  def writeCaptured(cs: Seq[Captured]): F[Unit] = influx.write(cs)
}

object InfluxDbClient extends StrictLogging {
  def apply(influx: InfluxClient[IO]): InfluxDbClient[IO] = new InfluxDbClient[IO](initialise(influx))

  private def initialise(influx: InfluxClient[IO]): InfluxClient[IO] = {
    val dbName = "ettl"
    influx.databaseExists(dbName).flatMap {
        case true =>
          logger.info(s"$dbName already exists. Using it!")
          IO.unit
        case false =>
          logger.info(s"$dbName does not exists. Creating it now!")
          influx.createDatabase(dbName)
    }.map(_ => influx.use(dbName)).unsafeRunSync()
  }
}
