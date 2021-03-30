package hu.szigyi.ettl.hal

import java.nio.file.{Path, Paths}
import scala.util.Try

class DummyCamera(testing: Boolean = false) extends GCamera {
  var savedImages: Seq[Path]                   = Seq.empty
  var adjustedCameraSettings: Map[String, Any] = Map.empty
  override def initialize: Try[Unit]           = Try(())
  override def newConfiguration: Try[GConfiguration] =
    Try(new GConfiguration {
      override def getNames: Seq[String] = adjustedCameraSettings.keys.toSeq
      override def setValue(name: String, value: Any): Try[Unit] = {
        adjustedCameraSettings = adjustedCameraSettings + (name -> value)
        Try(())
      }
      override def apply: Try[Unit] = Try(())
      override def close: Try[Unit] = Try(())
    })

  override def captureImage: Try[GFile] =
    Try(new GFile {
      override def close: Try[Unit] = Try(())

      override def saveImageTo(imageBasePath: Path): Try[Path] = {
        val imagePath = imageBasePath.resolve(imageNameGenerator)
        if (testing) {
          savedImages = savedImages :+ imagePath
          Try(imagePath)
        } else {
          generateNewImage(imagePath).map { _ =>
            savedImages = savedImages :+ imagePath
            imagePath
          }
        }
      }
    })

  override def close(): Try[Unit] = Try(())

  private var counter = 0
  private def imageNameGenerator: Path = {
    counter = counter + 1
    val counterName = f"$counter%04d"
    Paths.get(s"IMG_$counterName.JPG")
  }

  private def generateNewImage(imagePath: Path): Try[Unit] = {
    import javax.imageio.ImageIO
    import java.awt.image.BufferedImage
    import java.awt.Color
    import java.awt.Font

    val file  = imagePath.toFile
    val image = new BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB)
    val font  = new Font("Arial", Font.BOLD, 38)
    val g     = image.getGraphics
    g.setFont(font)
    g.setColor(Color.WHITE)
    g.drawString(file.getName, 20, 80)
    Try(ImageIO.write(image, "jpg", file))
  }
}
