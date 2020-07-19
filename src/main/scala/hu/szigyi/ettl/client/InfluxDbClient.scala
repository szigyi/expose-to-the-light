package hu.szigyi.ettl.client

import java.time.{Instant, ZoneOffset}
import java.time.format.DateTimeFormatter

import cats.effect.IO
import cats.Functor
import com.typesafe.scalalogging.StrictLogging
import hu.szigyi.ettl.client.InfluxDbClient.TimelapseTask
import reflux.{InfluxClient, Measurement, Read, TimeColumn, ToMeasurement}

class InfluxDbClient[F[_]: Functor](influx: InfluxClient[F]) {

  def getTimelapseTasks(from: Instant, to: Instant): F[Vector[TimelapseTask]] = {
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
         |from ${TimelapseTask.measurementName}
         |WHERE time >= '$from' AND time < '$to'""".stripMargin)
  }

  def writeTimelapseTasks(tlt: Seq[TimelapseTask]): F[Unit] = influx.write(tlt)
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

  trait InfluxDomainTrait {
    implicit def toTimeColumn(t: Instant): TimeColumn = TimeColumn(t)
    val dtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    def asString(str: String): String = s""""$str""""
    def asInt(n: Int): String = s"${n.toString}i"
    def asLong(l: Long): String = s"${l.toString}i"
    def asDouble(d: Double): String = d.toString
    def asBoolean(b: Boolean): String = b.toString
    def asOptionT[T](key: String, value: Option[T])(f: T => String): Option[(String, String)] = value.map(v => (key, f(v)))
    def asInstant(i: Instant): String = asString(dtFormat.format(i.atOffset(ZoneOffset.UTC)))
  }

  case class TimelapseTask(timestamp: TimeColumn,
                           id: String,
                           test: Boolean,
                           created: Instant,
                           shutterSpeed: Double,
                           iso: Int,
                           aperture: Double,
                           ev: Double)

  object TimelapseTask extends InfluxDomainTrait {
    val measurementName = "timelapse_task"
    val idFieldName = "id"
    val testFieldName = "test"
    val createdFieldName = "created"
    val shutterSpeedFieldName = "shutterSpeed"
    val isoFieldName = "iso"
    val apertureFieldName = "aperture"
    val evFieldName = "ev"

    implicit val reader: Read[TimelapseTask] = Read.instance(r =>
      TimelapseTask(r.time, r.getString(idFieldName), r.get[Boolean](testFieldName), r.get[Instant](createdFieldName),
        r.get[Double](shutterSpeedFieldName), r.get[Int](isoFieldName), r.get[Double](apertureFieldName),
        r.get[Double](evFieldName)))

    implicit val writer: ToMeasurement[TimelapseTask] = ToMeasurement.instance(measurementName,
      r => Measurement(
        Seq(
          createdFieldName -> asInstant(r.created),
          shutterSpeedFieldName -> asDouble(r.shutterSpeed),
          isoFieldName -> asInt(r.iso),
          apertureFieldName -> asDouble(r.aperture),
          evFieldName -> asDouble(r.ev)
        ),
        Seq(idFieldName -> r.id, testFieldName -> asBoolean(r.test)),
        time = Some(r.timestamp.time)
      ))
  }
}
