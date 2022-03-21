name := "smart-words"

version := "0.1"

scalaVersion := "3.1.1"

val Http4sVersion = "0.23.11"
val CirceVersion = "0.14.1"
libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "io.circe"        %% "circe-generic"       % CirceVersion,
)
