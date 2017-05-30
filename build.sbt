name := "Reserveon"
organization := "org.reserveon"
version := "1.0"
scalaVersion := "2.12.1"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies ++= {
  val circeVersion = "0.7.0"
  val slickVersion = "3.2.0"
  val akkaVersion = "10.0.5"

  Seq(
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",

    "com.typesafe.akka" %% "akka-http" % akkaVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.15.0",

    "org.flywaydb" % "flyway-core" % "3.0",
    "org.mindrot" % "jbcrypt" % "0.4",

    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.github.etaty" %% "rediscala" % "1.8.0",

    "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0",
    "com.nulab-inc" %% "akka-http-oauth2-provider" % "1.3.0",
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaVersion,
    "joda-time" % "joda-time" % "2.9.4",

    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,

    "org.scalactic" %% "scalactic" % "3.0.1" % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.specs2" %% "specs2-core" % "3.8.9" % "test",
    "org.specs2" %% "specs2-mock" % "3.8.9",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0" % "test"
  )
}

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.reserveon",
  scalaVersion := "2.12.1",
  test in assembly := {}
)

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerExposedPorts := Seq(9000)
dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")
