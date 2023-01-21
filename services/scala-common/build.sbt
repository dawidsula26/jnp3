val Http4sVersion = "1.0.0-M38"
val CirceVersion = "0.14.3"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"
val MongoCatsVersion = "0.6.6"
val PureConfigVersion = "0.17.2"
val FS2KafkaVersion = "3.0.0-M8"
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
      "com.github.fd4s"       %% "fs2-kafka"                 % FS2KafkaVersion,
      "com.nequissimus"       %% "circe-kafka"               % CirceKafkaVersion excludeAll(
        ExclusionRule("io.circe")
      )
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
