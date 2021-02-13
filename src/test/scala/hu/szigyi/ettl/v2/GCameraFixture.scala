package hu.szigyi.ettl.v2

import java.nio.file.Path
import scala.util.Try

object GCameraFixture {

  def capturedConfiguration: Try[GConfiguration] = Try(new GConfiguration {
    var map: Map[String, Any] = Map.empty
    override def getNames: Seq[String] = map.keys.toSeq
    override def setValue(name: String, value: Any): Try[Unit] = {
      map = map + (name -> value)
      Try()
    }
    override def apply: Try[Unit] = Try()
    override def close: Try[Unit] = Try()
  })

  def capturedFile: Try[GFile] = Try(new GFile {
    var list: Seq[Path] = Seq.empty
    override def close: Try[Unit] = Try()
    override def saveImageTo(imagePath: Path): Try[Path] = {
      list = list :+ imagePath
      Try(imagePath)
    }
  })
}
