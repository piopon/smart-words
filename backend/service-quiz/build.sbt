name := "service-quiz"
version := "0.1"
organization := "com.github.piopon"
licenses += ("MIT", url("https://github.com/piopon/smart-words/blob/main/LICENSE"))
homepage := Some(url("https://github.com/piopon/smart-words"))

scalaVersion := "2.13.6"
scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Classpaths.sbtPluginReleases

val Http4sVersion = "1.0.0-M21"
val CirceVersion = "0.14.5"
val Log4JVersion = "2.0.5"
val ScalaTestVersion = "3.2.15"
libraryDependencies ++= Seq(
  "org.slf4j"       %  "slf4j-api"           % Log4JVersion,
  "org.slf4j"       %  "slf4j-nop"           % Log4JVersion,
  "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
  "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "io.circe"        %% "circe-generic"       % CirceVersion,
  "io.circe"        %% "circe-literal"       % CirceVersion,
  "io.circe"        %% "circe-parser"        % CirceVersion,
  "org.scalatest"   %% "scalatest"           % ScalaTestVersion
)

coverageMinimumBranchTotal := 70
coverageFailOnMinimum := true

run := Defaults
  .runTask(
    Runtime / fullClasspath,
    Compile / run / mainClass,
    run / runner
  ).evaluated
cancelable in Global := true