package hu.szigyi.ettl.v2

import org.gphoto2.{Camera, CameraFile, CameraUtils, CameraWidgets}

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

  def setValue(name: String, value: Any): Unit

  def apply: Unit

  def close: Unit
}

class GConfigurationImpl(w: CameraWidgets) extends GConfiguration {

  import scala.jdk.CollectionConverters._

  override def getNames: Seq[String] = w.getNames.asScala.toSeq

  override def setValue(name: String, value: Any): Unit = w.setValue(name, value)

  override def apply: Unit = w.apply()

  override def close: Unit = CameraUtils.closeQuietly(w)
}

trait GFile {
  def close: Unit
  def saveImageTo(imagePath: Path): Try[Path]
}

class GFileImpl(f: CameraFile) extends GFile {
  override def close: Unit = CameraUtils.closeQuietly(f)

  //  val img: Array[Byte] = Files.readAllBytes(Paths.get(imagePath))
  //  val encoded: String = BaseEncoding.base64().encode(img)
  //  println(encoded)
  override def saveImageTo(imagePath: Path): Try[Path] =
    Try(f.save(imagePath.toAbsolutePath.toString)).map(_ => imagePath)
}
