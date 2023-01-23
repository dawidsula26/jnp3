val Http4sVersion = "1.0.0-M38"
val CirceVersion = "0.14.3"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"
val MongoCatsVersion = "0.6.6"
val PureConfigVersion = "0.17.2"
val KafkaStreamsVersion = "3.3.2"
val CirceKafkaVersion = "2.7.0"

lazy val someLib = ProjectRef(file("../scala-common"), "common")

lazy val root = (project in file("."))
  .dependsOn(someLib)
  .settings(
    organization := "stockrabbit",
    name := "statistics",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.10",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server"       % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client"       % Http4sVersion,
      "org.http4s"            %% "http4s-circe"              % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"                % Http4sVersion,
      "io.circe"              %% "circe-generic"             % CirceVersion,
      "io.circe"              %% "circe-parser"              % CirceVersion,
      "org.scalameta"         %% "munit"                     % MunitVersion           % Test,
      "org.typelevel"         %% "munit-cats-effect-3"       % MunitCatsEffectVersion % Test,
      "ch.qos.logback"        %  "logback-classic"           % LogbackVersion         % Runtime,
      "org.scalameta"         %% "svm-subs"                  % "20.2.0",
      "io.github.kirill5k"    %% "mongo4cats-core"           % MongoCatsVersion,
      "io.github.kirill5k"    %% "mongo4cats-embedded"       % MongoCatsVersion       % Test,
      "com.github.pureconfig" %% "pureconfig"                % PureConfigVersion,
      "org.apache.kafka"      %% "kafka-streams-scala"       % KafkaStreamsVersion,
      "com.nequissimus"       %% "circe-kafka"               % CirceKafkaVersion excludeAll(
        ExclusionRule("io.circe")
      )
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
