package hu.szigyi.ettl.v2.hal

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class GFileSpec extends AnyFreeSpec with Matchers {

  "can replace file extension to JPG" in {
    GFile.rawFileNameToJpg("_IMG_90989.CR2") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.NEF") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.DNG") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.png") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_IMG_90989.1") shouldBe "_IMG_90989.JPG"
    GFile.rawFileNameToJpg("_.extension") shouldBe "_.JPG"
  }
}
