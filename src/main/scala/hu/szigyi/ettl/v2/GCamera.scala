package hu.szigyi.ettl.v2

import org.gphoto2.{Camera, CameraFile, CameraUtils, CameraWidgets}

trait GCamera {
  def initialize(): Unit
  def newConfiguration(): GCameraConfiguration
  def captureImage(): CameraFile
}

class GCameraImpl() extends GCamera {
  private val c = new Camera()

  override def initialize(): Unit = c.initialize()

  override def newConfiguration(): GCameraConfiguration =
    new GCameraConfigurationImpl(c.newConfiguration())

  override def captureImage(): CameraFile = c.captureImage()
}

trait GCameraConfiguration {
  def getNames: Seq[String]

  def setValue(name: String, value: Any): Unit

  def apply(): Unit

  def close: Unit
}

class GCameraConfigurationImpl(w: CameraWidgets) extends GCameraConfiguration {
  import scala.jdk.CollectionConverters._

  override def getNames: Seq[String] = w.getNames.asScala.toSeq

  override def setValue(name: String, value: Any): Unit = w.setValue(name, value)

  override def apply(): Unit = w.apply()

  override def close: Unit = CameraUtils.closeQuietly(w)
}
