resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

addSbtPlugin("io.spray" %% "sbt-revolver" % "0.8.0")
addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.1.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4")