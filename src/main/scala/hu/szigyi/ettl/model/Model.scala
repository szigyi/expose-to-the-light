package hu.szigyi.ettl.model

object Model {

  case class SettingsCameraModel(shutterSpeed: Option[Double], shutterSpeedString: Option[String], iso: Option[Int], aperture: Option[Double])
  object SettingsCameraModel {
    def apply(shutterSpeed: Option[Double], iso: Option[Int], aperture: Option[Double]): SettingsCameraModel =
      new SettingsCameraModel(shutterSpeed, shutterSpeed.flatMap(ShutterSpeedMap.toShutterSpeed), iso, aperture)
  }
}
