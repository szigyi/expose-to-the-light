def env(name: String): String = sys.env.getOrElse(name, "unknown")
val buildNumber: String       = env("BUILD_NUMBER")

name := "expose-to-the-light"
organization := "hu.szigyi"
version := s"0.1.$buildNumber"
val scalaMajorVersion = "2.13"
scalaVersion := scalaMajorVersion + ".5"
resolvers += "jitpack" at "https://jitpack.io"
resolvers += ("baka.sk" at "http://www.baka.sk/maven2").withAllowInsecureProtocol(true)

mainClass in assembly := Some("hu.szigyi.ettl.app.CliEttlApp")
assemblyJarName in assembly := "expose-to-the-light_" + scalaMajorVersion + "-" + version.value + ".jar"

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked"
)

libraryDependencies ++= Seq(
  "ch.qos.logback"             % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2",
  "com.github.pureconfig"      %% "pureconfig"     % "0.14.1",
  "org.rogach"                 %% "scallop"        % "4.0.2",
  "org.gphoto"                 % "gphoto2-java"    % "1.5",
  "org.typelevel"              %% "cats-effect"    % "2.3.3",
  "org.scalatest"              %% "scalatest"      % "3.2.5" % Test
)
