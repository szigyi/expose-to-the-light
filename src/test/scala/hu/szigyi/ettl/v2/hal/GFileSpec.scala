package hu.szigyi.ettl.v2.hal

import hu.szigyi.ettl.v2.hal.GFile.saveTo
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths

class GFileSpec extends AnyFreeSpec with Matchers {

  "can replace file extension to JPG" in {
    GFile.rawFileNameToJpg("_IMG_90989.CR2") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.NEF") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.DNG") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.png") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.1") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_.extension") shouldBe "_.JPG"
  }

  "should use the original file name from gphoto2 's Path" in {
    var savedWasInvoked = false
    def saving(f: String): Unit = savedWasInvoked = true
    saveTo(Paths.get("/home/usr/pi/pictures"), "_IMG_2.CR2", saving)
      .get
      .toAbsolutePath
      .toString shouldBe "/home/usr/pi/pictures/_IMG_2.CR2"

    savedWasInvoked shouldBe true
  }
}
