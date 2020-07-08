package hu.szigyi.ettl.service

import org.gphoto2.{Camera, CameraUtils, CameraWidgets}

class CameraService {

  def useCamera(doYourThing: CameraWidgets => Unit): Unit = {
    val camera = new Camera()
    try {
      val rootWidget: CameraWidgets = camera.newConfiguration()
      doYourThing(rootWidget)
    } finally {
      CameraUtils.closeQuietly(camera)
    }
  }

  def setSettings(rootWidget: CameraWidgets, shutterSpeed: Double, iso: Int, aperture: Double): Unit = {
    rootWidget.setValue("shutterspeed", shutterSpeed)
    rootWidget.setValue("iso", iso)
    rootWidget.setValue("aperture", aperture)
  }
}
