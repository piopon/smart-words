resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")
addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.3.8")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")