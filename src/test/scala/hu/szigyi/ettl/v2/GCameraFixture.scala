package hu.szigyi.ettl.v2

import java.nio.file.Path
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

  class DummyCamera extends GCamera {
    var savedImages: Seq[Path] = Seq.empty
    var adjustedCameraSettings: Map[String, Any] = Map.empty
    override def initialize: Try[Unit] = Try()
    override def newConfiguration: Try[GConfiguration] = Try(new GConfiguration {
      override def getNames: Seq[String] = adjustedCameraSettings.keys.toSeq
      override def setValue(name: String, value: Any): Try[Unit] = {
        adjustedCameraSettings = adjustedCameraSettings + (name -> value)
        Try()
      }
      override def apply: Try[Unit] = Try()
      override def close: Try[Unit] = Try()
    })

    override def captureImage: Try[GFile] = Try(new GFile {
      override def close: Try[Unit] = Try()
      override def saveImageTo(imagePath: Path): Try[Path] = {
        savedImages = savedImages :+ imagePath
        Try(imagePath)
      }
    })
  }
}
