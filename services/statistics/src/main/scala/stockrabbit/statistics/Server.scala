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
import org.http4s.server.Server
import scala.annotation.unused
import stockrabbit.statistics.kafka.KafkaInput

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

  def makeConfig(setupVersion: SetupVersion): IO[Config] = {
    val mainConfigFileSource = IO.blocking(Source.fromResource(setupVersion.configFileName))
    val mainConfigResource = Resource.make(mainConfigFileSource)(src => IO(src.close()))

    val defaultConfigFileSource = IO.blocking(Source.fromResource(setupVersion.backupFileName))
    val defaultConfigResource = Resource.make(defaultConfigFileSource)(src => IO(src.close()))
    for {
      mainConfigString <- mainConfigResource.use(src => IO.blocking(src.getLines().mkString("\n")))
      mainConfigSource = ConfigSource.string(mainConfigString)

      defaultConfigString <- defaultConfigResource.use(src => IO.blocking(src.getLines().mkString("\n")))
      defaultConfigSource = ConfigSource.string(defaultConfigString)

      config = mainConfigSource.withFallback(defaultConfigSource).loadOrThrow[Config]
    } yield (config)
  }

  def makeEnv[F[_]: Async](config: Config): Resource[F, Environment[F]] = {
    Environment.impl[F](config)
  }

  def makeServer[F[_]: Async](env: Environment[F]): Resource[F, Server] = {
    val readerAlg = Reader.impl[F](env)
    val managerAlg = Manager.impl[F](env)

    val httpApp = (
      reader.Routes.routes[F](readerAlg) <+> 
        reader.Routes.routes[F](readerAlg) <+> 
      reader.Routes.routes[F](readerAlg) <+> 
      manager.Routes.routes[F](managerAlg)
    ).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
      
    EmberServerBuilder.default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(env.server.port).get)
      .withHttpApp(finalHttpApp)
      .build
  }

  def makeKafka(@unused env: Environment[IO]): IO[Unit] = {
    for {
      _ <- KafkaInput.run(env).start
    } yield ()
  }

  def run: IO[Nothing] = {
    implicit val ioAsync = IO.asyncForIO
    val resources = for {
      setupVersion <- Resource.eval(getSetupVersion())
      config <- Resource.eval(makeConfig(setupVersion))
      env <- makeEnv(config)
      _ <- makeServer(env)
      _ <- Resource.eval(makeKafka(env))
    } yield ()
    resources.useForever
  }
}
