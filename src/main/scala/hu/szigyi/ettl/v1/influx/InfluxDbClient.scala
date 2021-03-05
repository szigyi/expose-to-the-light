package hu.szigyi.ettl.v1.influx

import java.time.Instant

import cats.Functor
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.v1.influx.InfluxDomain.{CapturedDomain, KeyFrameDomain, ToSetSettingDomain, ToCaptureDomain}
import reflux._

class InfluxDbClient[F[_]: Functor](influx: InfluxClient[F]) {

  def getSettings(from: Instant, to: Instant): F[Vector[ToSetSettingDomain]] =
    influx.asVector[ToSetSettingDomain](
      s"""
         |SELECT
         |${ToSetSettingDomain.idFieldName},
         |${ToSetSettingDomain.testFieldName},
         |${ToSetSettingDomain.createdFieldName},
         |${ToSetSettingDomain.shutterSpeedFieldName},
         |${ToSetSettingDomain.isoFieldName},
         |${ToSetSettingDomain.apertureFieldName},
         |${ToSetSettingDomain.evFieldName}
         |FROM ${ToSetSettingDomain.measurementName}
         |WHERE time >= '$from' AND time < '$to'""".stripMargin)

  def getTicks(from: Instant, to: Instant): F[Vector[ToCaptureDomain]] =
    influx.asVector[ToCaptureDomain](
      s"""
         |SELECT
         |${ToCaptureDomain.idFieldName},
         |${ToCaptureDomain.photoOrderFieldName}
         |FROM ${ToCaptureDomain.measurementName}
         |WHERE time >= '$from' AND time < '$to'""".stripMargin)

  def getCaptured(from: Instant): F[Vector[CapturedDomain]] =
    influx.asVector[CapturedDomain](
      s"""
         |SELECT
         |${CapturedDomain.idFieldName},
         |${CapturedDomain.testFieldName},
         |${CapturedDomain.photoOrderFieldName},
         |${CapturedDomain.shutterSpeedFieldName},
         |${CapturedDomain.isoFieldName},
         |${CapturedDomain.apertureFieldName},
         |${CapturedDomain.evFieldName},
         |${CapturedDomain.errorFieldName},
         |${CapturedDomain.suggestionFieldName}
         |FROM ${CapturedDomain.measurementName}
         |WHERE time >= '$from'""".stripMargin)

  def getKeyFrameIds: F[Vector[String]] =
    influx.asVector(
      s"""
         |SELECT
         |${KeyFrameDomain.idFieldName},
         |${KeyFrameDomain.shutterSpeedStringFieldName}
         |FROM ${KeyFrameDomain.measurementName}
         |GROUP BY ${KeyFrameDomain.idFieldName}
         |LIMIT 1""".stripMargin)

  def getKeyFrames(id: String): F[Vector[KeyFrameDomain]] =
    influx.asVector[KeyFrameDomain](
      s"""
         |SELECT
         |${KeyFrameDomain.idFieldName},
         |"${KeyFrameDomain.durationFieldName}",
         |${KeyFrameDomain.shutterSpeedFieldName},
         |${KeyFrameDomain.shutterSpeedStringFieldName},
         |${KeyFrameDomain.isoFieldName},
         |${KeyFrameDomain.apertureFieldName},
         |${KeyFrameDomain.evFieldName}
         |FROM ${KeyFrameDomain.measurementName}
         |WHERE ${KeyFrameDomain.idFieldName}='$id'""".stripMargin)

  def writeSettings(tlt: Seq[ToSetSettingDomain]): F[Unit] = influx.write(tlt)
  def writeCaptured(cs: Seq[CapturedDomain]): F[Unit] = influx.write(cs)
  def writeKeyFrame(kfs: Seq[KeyFrameDomain]): F[Unit] = influx.write(kfs)
  def writeToBeCaptured(cs: Seq[ToCaptureDomain]): F[Unit] = influx.write(cs)
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
