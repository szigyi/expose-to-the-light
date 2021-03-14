package hu.szigyi.ettl.v2.hal

import java.nio.file.Path
import scala.util.Try

class DummyCamera(testing: Boolean = false) extends GCamera {
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
    override def saveImageTo(imagePath: Path): Try[Path] =
    if (testing) {
      savedImages = savedImages :+ imagePath
      Try(imagePath)
    } else {
      generateNewImage(imagePath).map { _ =>
        savedImages = savedImages :+ imagePath
        imagePath
      }
    }
  })

  private def generateNewImage(imagePath: Path): Try[Unit] = {
    import javax.imageio.ImageIO
    import java.awt.image.BufferedImage
    import java.awt.Color
    import java.awt.Font

    val file = imagePath.toFile
    val image = new BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB)
    val font = new Font("Arial", Font.BOLD, 38)
    val g = image.getGraphics
    g.setFont(font)
    g.setColor(Color.WHITE)
    g.drawString(file.getName, 20, 80)
    Try(ImageIO.write(image, "jpg", file))
  }
}