package stockrabbit.statistics.environment.general

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
}


