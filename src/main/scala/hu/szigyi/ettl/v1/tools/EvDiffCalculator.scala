package hu.szigyi.ettl.v1.tools

import java.math.MathContext

import hu.szigyi.ettl.v1.service.EvService
import hu.szigyi.ettl.v1.util.{ApertureMap, IsoMap, ShutterSpeedMap}

object EvDiffCalculator extends App {

  case class EVSetting(shutterSpeed: Double, iso: Int, aperture: Double, ev: Double) {
    def toStringToPrint: String = {
      s"${ShutterSpeedMap.toShutterSpeed(shutterSpeed).get},\t$iso,\t$aperture"
    }
  }
  object EVSetting {
    def apply(shutterSpeed: Double, iso: Int, aperture: Double): EVSetting = {
      val evRaw = EvService.ev(shutterSpeed, iso, aperture)
      val ev = BigDecimal(evRaw).setScale(2, BigDecimal.RoundingMode.HALF_UP).rounded.toDouble
      new EVSetting(shutterSpeed, iso, aperture, ev)
    }
  }

  def generate(shutterSpeeds: Seq[Double], isos: Seq[Int], apertures: Seq[Double]): Seq[EVSetting] =
    for {
      ss <- shutterSpeeds
      iso <- isos
      aperture <- apertures
    } yield EVSetting(ss, iso, aperture)

  val settings = generate(ShutterSpeedMap.getAll, IsoMap.getAll, ApertureMap.getAll)
  val mapSettings = settings.groupBy(_.ev)
  val keys = mapSettings.keys.toSeq.sorted

//  keys.map(key => {
//    println(
//      s"""
//         |$key
//         |${mapSettings(key).map(_.toStringToPrint).mkString("\n")}""".stripMargin)
//  })

  println("#########")
  def maxSettingsDifference(s: Seq[EVSetting]): Double = {
    val shutterSpeedDiff = s.maxBy(_.shutterSpeed).shutterSpeed - s.minBy(_.shutterSpeed).shutterSpeed
    val isoDiff = s.maxBy(_.iso).iso - s.minBy(_.iso).iso
    val apertureDiff = s.maxBy(_.aperture).aperture - s.minBy(_.aperture).aperture
    shutterSpeedDiff * isoDiff * apertureDiff
  }

  val biggestDynamicChange = mapSettings.valuesIterator.reduce[Seq[EVSetting]] {
    case (left, right) =>
      if (maxSettingsDifference(left) > maxSettingsDifference(right)) left
      else right
  }
  println(biggestDynamicChange.mkString("", ",\n", ""))
}
