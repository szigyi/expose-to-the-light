import java.util.jar.Attributes.Name

import sbt.io.IO.zip
import Path.flatRebase

def env(name: String) = Option(System.getenv(name)).getOrElse("unknown")
//val buildNumber: String = env("BUILD_NUMBER")
val buildNumber: String = "1"

name := "expose-to-the-light"
organization := "hu.szigyi"
version := s"0.1.$buildNumber"
scalaVersion := "2.13.3"
resolvers += "jitpack" at "https://jitpack.io"
resolvers += ("baka.sk" at "http://www.baka.sk/maven2").withAllowInsecureProtocol(true)

mainClass in assembly := Some("hu.szigyi.ettl.App")

val circeVersion  = "0.13.0"
val http4sVersion = "0.21.0"

libraryDependencies ++= Seq(
  "io.circe"                  %% "circe-generic"      % circeVersion,
  "io.circe"                  %% "circe-parser"       % circeVersion,
  "io.circe"                  %% "circe-literal"      % circeVersion,
  "com.github.pureconfig"     %% "pureconfig"         % "0.12.2",

  "org.gphoto"                %  "gphoto2-java"       % "1.5",
  "com.github.dorinp.reflux"  %% "reflux-generic"     % "0.0.13",
  "org.apache.commons"        % "commons-text"        % "1.8",

  "org.http4s"                %% "http4s-circe"       % http4sVersion,
  "org.http4s"                %% "http4s-dsl"         % http4sVersion,
  "org.http4s"                %% "http4s-blaze-server" % http4sVersion,

  "ch.qos.logback"            %  "logback-classic"    % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"     % "3.9.2",

  "org.scalatest"             %% "scalatest"          % "3.1.0"  % Test
)

//publishArtifact := false

publishMavenStyle := true
githubOwner := "szigyi"
githubRepository := "expose-to-the-light"
githubTokenSource := TokenSource.GitConfig("github.token")  || TokenSource.Environment("GITHUB_TOKEN")

credentials +=
  Credentials(
    "GitHub Package Registry",
    "https://maven.pkg.github.com",
    "szigyi",
    sys.env.getOrElse("GITHUB_TOKEN", "N/A")
  )