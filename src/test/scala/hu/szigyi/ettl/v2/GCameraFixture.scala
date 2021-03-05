package hu.szigyi.ettl.v2

import hu.szigyi.ettl.v2.hal.GConfiguration

import scala.util.Try

object GCameraFixture {

  def capturedConfiguration: Try[GConfiguration] = Try(new GConfiguration {
    var adjustedCameraSettings: Map[String, Any] = Map.empty
    override def getNames: Seq[String] = adjustedCameraSettings.keys.toSeq
    override def setValue(name: String, value: Any): Try[Unit] = {
      adjustedCameraSettings = adjustedCameraSettings + (name -> value)
      Try()
    }
    override def apply: Try[Unit] = Try()
    override def close: Try[Unit] = Try()
  })
}
