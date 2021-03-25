package hu.szigyi.ettl.service

import java.nio.file.Path
import scala.util.{Failure, Success, Try}

class DirectoryService {

  def createFolder(folderPath: Path): Try[Unit] =
    folderPath.toFile.mkdirs() match {
      case true =>
        Success(())
      case false =>
        Failure(new Exception(s"Could not create folder: $folderPath"))
    }

}
