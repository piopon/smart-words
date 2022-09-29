name := "smart-words"

version := "0.1"

scalaVersion := "2.13.6"

val Http4sVersion = "1.0.0-M21"
val CirceVersion = "0.14.3"
libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "io.circe"        %% "circe-generic"       % CirceVersion,
  "io.circe"        %% "circe-literal"       % CirceVersion,
  "io.circe"        %% "circe-parser"        % CirceVersion,
)
