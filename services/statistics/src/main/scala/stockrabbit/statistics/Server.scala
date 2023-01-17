package stockrabbit.statistics

import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.environment.general.Config
import stockrabbit.statistics.manager.Manager
import stockrabbit.statistics.reader.Reader

import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import pureconfig._
import pureconfig.generic.auto._
import scala.io.Source
import stockrabbit.statistics.environment.general.SetupVersion

object StatisticsServer {

  def getSetupVersion(): IO[SetupVersion] = {
    val possibleVersionsMap = SetupVersion.versions.map(v => v.name -> v).toMap
    for {
      env <- IO(sys.env)
      versionNameOption <- IO(env.get(SetupVersion.setupVersionVariableName))
      versionName = versionNameOption.getOrElse(SetupVersion.Local.name)
      version = possibleVersionsMap.get(versionName).get
    } yield (version)
  }

  def getConfig(setupVersion: SetupVersion): IO[Config] = {
    val mainConfigFileSource = IO(Source.fromResource(setupVersion.configFileName))
    val mainConfigResource = Resource.make(mainConfigFileSource)(src => IO(src.close()))

    val defaultConfigFileSource = IO(Source.fromResource(setupVersion.backupFileName))
    val defaultConfigResource = Resource.make(defaultConfigFileSource)(src => IO(src.close()))
    for {
      mainConfigString <- mainConfigResource.use(src => IO(src.getLines().mkString("\n")))
      mainConfigSource = ConfigSource.string(mainConfigString)

      defaultConfigString <- defaultConfigResource.use(src => IO(src.getLines().mkString("\n")))
      defaultConfigSource = ConfigSource.string(defaultConfigString)

      config = mainConfigSource.withFallback(defaultConfigSource).loadOrThrow[Config]
    } yield (config)
  }

  def getResources[F[_]: Async](config: Config): F[Nothing] = {
    val resources = for {
      env <- Environment.impl(config)

      readerAlg = Reader.impl[F](env)
      managerAlg = Manager.impl[F](env)

      httpApp = (
        reader.Routes.routes[F](readerAlg) <+> 
        manager.Routes.routes[F](managerAlg)
      ).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      
      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
    resources.useForever
  }

  def run: IO[Nothing] = {
    implicit val asyncForIO = IO.asyncForIO
    val setupVersion = getSetupVersion()
    val config = setupVersion.flatMap(getConfig(_))
    config.flatMap(getResources(_))
  }
}
