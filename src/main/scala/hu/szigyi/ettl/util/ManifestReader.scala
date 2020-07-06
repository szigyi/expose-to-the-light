package hu.szigyi.ettl.util

import java.net.URL

import hu.szigyi.ettl.util.ManifestReader.ManifestInfo

import scala.util.Try

class ManifestReader(clazz: Class[_]) {

  private val manifest = Try {
    val url         = clazz.getResource(s"/${clazz.getName.replace('.', '/')}.class").toString
    val manifestUrl = url.substring(0, url.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF"
    new java.util.jar.Manifest(new URL(manifestUrl).openStream())
  }.toOption

  def manifestInfo() = ManifestInfo(
    buildNumber = stringFromManifest("Implementation-Version"),
    buildTimeStamp = stringFromManifest("Build-Timestamp"),
    gitHash = stringFromManifest("Git-Hash"),
  )

  private def stringFromManifest(key: String) = manifest.flatMap(m => Option(m.getMainAttributes.getValue(key)))

}

object ManifestReader {
  def apply(clazz: Class[_]): ManifestReader = new ManifestReader(clazz)

  case class ManifestInfo(buildNumber: Option[String], buildTimeStamp: Option[String], gitHash: Option[String])
}
