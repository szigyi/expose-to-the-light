package hu.szigyi.ettl.client.influx

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

import hu.szigyi.ettl.service.EvService
import reflux.{Measurement, Read, TimeColumn, ToMeasurement}

import scala.concurrent.duration.Duration

object InfluxDomain {
  case class TimelapseTask(timestamp: TimeColumn,
                           id: String,
                           test: Boolean,
                           created: Instant,
                           shutterSpeed: Option[Double],
                           iso: Option[Int],
                           aperture: Option[Double],
                           ev: Option[Double])
  case class Captured(timestamp: TimeColumn,
                      id: String,
                      test: Boolean,
                      shutterSpeed: Double,
                      iso: Int,
                      aperture: Double,
                      ev: Double,
                      error: Option[String],
                      suggestion: Option[String])

  case class KeyFrame(timestamp: TimeColumn,
                      id: String,
                      duration: Duration,
                      shutterSpeed: Double,
                      shutterSpeedString: String,
                      iso: Int,
                      aperture: Double,
                      ev: Double)

  trait InfluxDomainTrait {
    val measurementName: String
    implicit def toTimeColumn(t: Instant): TimeColumn = TimeColumn(t)
    val dtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    def asString(str: String): String = s""""$str""""
    def asInt(n: Int): String = s"${n.toString}i"
    def asLong(l: Long): String = s"${l.toString}i"
    def asDouble(d: Double): String = d.toString
    def asBoolean(b: Boolean): String = b.toString
    def asOptionT[T](key: String, value: Option[T])(f: T => String): Option[(String, String)] = value.map(v => (key, f(v)))
    def asInstant(i: Instant): String = asString(dtFormat.format(i.atOffset(ZoneOffset.UTC)))
    def toInstant(str: String): Instant = Instant.parse(str)
  }

  object TimelapseTask extends InfluxDomainTrait {
    override val measurementName = "timelapse_task"
    val idFieldName = "id"
    val testFieldName = "test"
    val createdFieldName = "created"
    val shutterSpeedFieldName = "shutterSpeed"
    val isoFieldName = "iso"
    val apertureFieldName = "aperture"
    val evFieldName = "ev"

    implicit val reader: Read[TimelapseTask] = Read.instance(r =>
      TimelapseTask(r.time, r.getString(idFieldName), r.getString(testFieldName).toBoolean, r.get[Instant](createdFieldName),
        r.getOption(shutterSpeedFieldName).map(_.toDouble), r.getOption(isoFieldName).map(_.toInt),
        r.getOption(apertureFieldName).map(_.toDouble), r.getOption(evFieldName).map(_.toDouble)))

    implicit val writer: ToMeasurement[TimelapseTask] = ToMeasurement.instance(measurementName,
      r => Measurement(
        Seq(createdFieldName -> asInstant(r.created))
          ++ asOptionT(shutterSpeedFieldName, r.shutterSpeed)(asDouble)
          ++ asOptionT(isoFieldName, r.iso)(asInt)
          ++ asOptionT(apertureFieldName, r.aperture)(asDouble)
          ++ asOptionT(evFieldName, r.ev)(asDouble)
        ,
        Seq(idFieldName -> r.id, testFieldName -> asBoolean(r.test)),
        time = Some(r.timestamp.time)
      ))
  }

  object Captured extends InfluxDomainTrait {
    override val measurementName = "captured"
    val idFieldName = "id"
    val testFieldName = "test"
    val shutterSpeedFieldName = "shutterSpeed"
    val isoFieldName = "iso"
    val apertureFieldName = "aperture"
    val evFieldName = "ev"
    val errorFieldName = "error"
    val suggestionFieldName = "suggestion"

    implicit val reader: Read[Captured] = Read.instance(r =>
      Captured(r.time, r.getString(idFieldName), r.getString(testFieldName).toBoolean, r.get[Double](shutterSpeedFieldName),
        r.get[Int](isoFieldName), r.get[Double](apertureFieldName), r.get[Double](evFieldName),
        r.getOption(errorFieldName), r.getOption(suggestionFieldName)))

    implicit val writer: ToMeasurement[Captured] = ToMeasurement.instance(measurementName,
      r => Measurement(
        Seq(
          shutterSpeedFieldName -> asDouble(r.shutterSpeed),
          isoFieldName -> asInt(r.iso),
          apertureFieldName -> asDouble(r.aperture),
          evFieldName -> asDouble(r.ev)
        )
          ++ asOptionT(errorFieldName, r.error)(asString)
        // TODO fix it, there is an issue with new line char in the string, remove it either here or ehn you create the error message.
          ++ asOptionT(suggestionFieldName, r.suggestion)(asString)
        ,
        Seq(idFieldName -> r.id, testFieldName -> asBoolean(r.test)),
        time = Some(r.timestamp.time)
      ))
  }

  object KeyFrame extends InfluxDomainTrait {
    override val measurementName = "key_frame"
    val idFieldName = "id"
    val durationFieldName = "duration"
    val shutterSpeedFieldName = "shutterSpeed"
    val shutterSpeedStringFieldName = "shutterSpeedString"
    val isoFieldName = "iso"
    val apertureFieldName = "aperture"
    val evFieldName = "ev"

    implicit val reader: Read[KeyFrame] = Read.instance(r =>
      KeyFrame(r.time, r.getString(idFieldName), Duration.fromNanos(r.get[Long](durationFieldName)),
        r.get[Double](shutterSpeedFieldName), r.getString(shutterSpeedStringFieldName), r.get[Int](isoFieldName),
        r.get[Double](apertureFieldName), r.get[Double](evFieldName)))

    implicit val writer: ToMeasurement[KeyFrame] = ToMeasurement.instance(measurementName,
      r => Measurement(
        Seq(
          durationFieldName -> asLong(r.duration.toNanos),
          shutterSpeedFieldName -> asDouble(r.shutterSpeed),
          shutterSpeedStringFieldName -> asString(r.shutterSpeedString),
          isoFieldName -> asInt(r.iso),
          apertureFieldName -> asDouble(r.aperture),
          evFieldName -> asDouble(r.ev)
        ),
        Seq(idFieldName -> r.id),
        time = Some(r.timestamp.time)
      ))

    def apply(timestamp: TimeColumn, id: String, duration: Duration, shutterSpeed: Double, shutterSpeedString: String, iso: Int, aperture: Double): KeyFrame =
      new KeyFrame(timestamp, id, duration, shutterSpeed, shutterSpeedString, iso, aperture, EvService.ev(shutterSpeed, iso, aperture))
  }
}
