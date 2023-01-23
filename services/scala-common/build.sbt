val Http4sVersion = "1.0.0-M38"
val CirceVersion = "0.14.3"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"
val PureConfigVersion = "0.17.2"
val KafkaStreamsVersion = "3.3.2"
val CatsEffectsVersion = "3.4.5"
val CirceKafkaVersion = "2.7.0"

lazy val common = (project in file("."))
  .settings(
    organization := "stockrabbit",
    name := "common",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.10",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "io.circe"              %% "circe-generic"             % CirceVersion,
      "io.circe"              %% "circe-parser"              % CirceVersion,
      "org.scalameta"         %% "svm-subs"                  % "20.2.0",
      "com.github.pureconfig" %% "pureconfig"                % PureConfigVersion,
      "org.apache.kafka"      %% "kafka-streams-scala"       % KafkaStreamsVersion,
      "org.typelevel"         %% "cats-effect"               % CatsEffectsVersion,
      "com.nequissimus"       %% "circe-kafka"               % CirceKafkaVersion excludeAll(
        ExclusionRule("io.circe")
      )
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
