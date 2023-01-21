package stockrabbit.statistics.environment.general

import cats.effect._

sealed trait SetupVersion {
  def name: String
  def configFileName: String = name + ".conf"
  def backupFileName: String = "application.conf"
}

object SetupVersion {
  val setupVersionVariableName = "SETUP_VERSION"
  val versions = Seq(Local, DockerTest, DockerProd)

  object Local extends SetupVersion {
    def name = "local"
  }
  object DockerTest extends SetupVersion {
    def name = "dockerTest"
  }
  object DockerProd extends SetupVersion {
    def name = "dockerProd"
  }

  def impl(): IO[SetupVersion] = {
    val possibleVersionsMap = SetupVersion.versions.map(v => v.name -> v).toMap
    for {
      env <- IO(sys.env)
      versionNameOption <- IO(env.get(SetupVersion.setupVersionVariableName))
      versionName = versionNameOption.getOrElse(SetupVersion.Local.name)
      version = possibleVersionsMap.get(versionName).get
    } yield (version)
  }
}


