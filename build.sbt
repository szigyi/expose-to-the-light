
def env(name: String): String = sys.env.get(name).getOrElse("unknown")
val buildNumber: String = env("BUILD_NUMBER")

name := "expose-to-the-light"
organization := "hu.szigyi"
version := s"0.1.$buildNumber"
val scalaMajorVersion = "2.13"
scalaVersion := scalaMajorVersion + ".5"
resolvers += "jitpack" at "https://jitpack.io"
resolvers += ("baka.sk" at "http://www.baka.sk/maven2").withAllowInsecureProtocol(true)

mainClass in assembly := Some("hu.szigyi.ettl.v2.app.CliApp")
assemblyJarName in assembly := "expose-to-the-light_" + scalaMajorVersion + "-" + version.value + ".jar"

scalacOptions += "-deprecation"

val circeVersion  = "0.13.0"
val http4sVersion = "0.21.0"

libraryDependencies ++= Seq(
  "io.circe"                  %% "circe-generic"      % circeVersion,
  "io.circe"                  %% "circe-parser"       % circeVersion,
  "io.circe"                  %% "circe-literal"      % circeVersion,
  "com.github.pureconfig"     %% "pureconfig"         % "0.12.2",

  "org.gphoto"                %  "gphoto2-java"       % "1.5",
  "com.github.dorinp.reflux"  %% "reflux-generic"     % "0.0.13",
  "org.apache.commons"        %  "commons-text"       % "1.8",
  "com.google.jimfs"          %  "jimfs"              % "1.1",
  "org.rogach"                %% "scallop"            % "4.0.2",

  "org.http4s"                %% "http4s-circe"       % http4sVersion,
  "org.http4s"                %% "http4s-dsl"         % http4sVersion,
  "org.http4s"                %% "http4s-blaze-server" % http4sVersion,

  "ch.qos.logback"            %  "logback-classic"    % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"     % "3.9.2",

  "org.scalatest"             %% "scalatest"          % "3.1.0"  % Test
)