package hu.szigyi.ettl.v2.hal

import org.gphoto2.{Camera, CameraFile, CameraWidgets}

import java.nio.file.Path
import scala.util.Try

trait GCamera {
  def initialize: Try[Unit]
  def newConfiguration: Try[GConfiguration]
  def captureImage: Try[GFile]
}

class GCameraImpl extends GCamera {
  private val c = new Camera

  override def initialize: Try[Unit] =
    Try(c.initialize())

  override def newConfiguration: Try[GConfiguration] =
    Try(c.newConfiguration()).map(new GConfigurationImpl(_))

  override def captureImage: Try[GFile] =
    Try(c.captureImage()).map(new GFileImpl(_))
}

trait GConfiguration {
  def getNames: Seq[String]
  def setValue(name: String, value: Any): Try[Unit]
  def apply: Try[Unit]
  def close: Try[Unit]
}

class GConfigurationImpl(w: CameraWidgets) extends GConfiguration {
  import scala.jdk.CollectionConverters._

  override def getNames: Seq[String] =
    w.getNames.asScala.toSeq
  override def setValue(name: String, value: Any): Try[Unit] =
    Try(w.setValue(name, value))
  override def apply: Try[Unit] =
    Try(w.apply())
  override def close: Try[Unit] =
    Try(w.close)
}

trait GFile {
  def close: Try[Unit]
  def saveImageTo(imagePath: Path): Try[Path]
}

object GFile {
  // https://stackoverflow.com/a/4731270
  def rawFileNameToJpg(rawName: String): String =
    rawName.replaceAll("\\.[^.]*$", "") + ".JPG"
}

class GFileImpl(f: CameraFile) extends GFile {
  override def close: Try[Unit] =
    Try(f.close)
  override def saveImageTo(imagePath: Path): Try[Path] =
    Try(f.save(imagePath.toAbsolutePath.toString)).map(_ => imagePath)
}