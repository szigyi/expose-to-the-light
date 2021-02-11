package hu.szigyi.ettl.v2

import com.google.common.io.BaseEncoding
import org.gphoto2.{Camera, CameraFile, CameraUtils, CameraWidgets}

import java.nio.file.{Files, Paths}
import scala.util.Try

trait GCamera {
  def initialize(): Try[Unit]
  def newConfiguration(): Try[GConfiguration]
  def captureImage(): Try[GFile]
}

class GCameraImpl extends GCamera {
  private val c = new Camera()

  override def initialize(): Try[Unit] =
    Try(c.initialize())

  override def newConfiguration(): Try[GConfiguration] =
    Try(c.newConfiguration()).map(new GConfigurationImpl(_))

  override def captureImage(): Try[GFile] =
    Try(c.captureImage()).map(new GFileImpl(_))
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
  def getImage: Try[Array[Byte]]
}

class GFileImpl(f: CameraFile) extends GFile {
  override def close: Unit = CameraUtils.closeQuietly(f)

  override def getImage: Try[Array[Byte]] = {
    val imagePath = "/Users/szabolcs/dev/expose-to-the-light/src/main/resources/get_it_done.cr2"
    Try(f.save(imagePath)).map { _ =>
      val img: Array[Byte] = Files.readAllBytes(Paths.get(imagePath))
      val encoded: String = BaseEncoding.base64().encode(img)
      println(encoded)
      img
    }
  }
}
