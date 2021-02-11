package hu.szigyi.ettl.v2

import org.gphoto2.{Camera, CameraFile, CameraUtils, CameraWidgets}

trait GCamera {
  def initialize(): Unit
  def newConfiguration(): GConfiguration
  def captureImage(): GFile
}

class GCameraImpl extends GCamera {
  private val c = new Camera()

  override def initialize(): Unit =
    c.initialize()

  override def newConfiguration(): GConfiguration =
    new GConfigurationImpl(c.newConfiguration())

  override def captureImage(): GFile =
    new GFileImpl(c.captureImage())
}

trait GConfiguration {
  def getNames: Seq[String]

  def setValue(name: String, value: Any): Unit

  def apply(): Unit

  def close: Unit
}

class GConfigurationImpl(w: CameraWidgets) extends GConfiguration {
  import scala.jdk.CollectionConverters._

  override def getNames: Seq[String] = w.getNames.asScala.toSeq

  override def setValue(name: String, value: Any): Unit = w.setValue(name, value)

  override def apply(): Unit = w.apply()

  override def close: Unit = CameraUtils.closeQuietly(w)
}

trait GFile {
  def close: Unit
}

class GFileImpl(f: CameraFile) extends GFile {
  override def close: Unit = CameraUtils.closeQuietly(f)
}
