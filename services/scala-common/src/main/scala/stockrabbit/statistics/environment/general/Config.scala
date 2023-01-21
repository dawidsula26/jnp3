package stockrabbit.statistics.environment.general

import cats.effect._
import scala.io.Source
import pureconfig.ConfigSource
import scala.reflect.ClassTag
import pureconfig.ConfigReader

object Config {
  def impl[Conf: ClassTag: ConfigReader](setupVersion: SetupVersion): IO[Conf] = {
    val mainConfigFileSource = IO.blocking(Source.fromResource(setupVersion.configFileName))
    val mainConfigResource = Resource.make(mainConfigFileSource)(src => IO(src.close()))

    val defaultConfigFileSource = IO.blocking(Source.fromResource(setupVersion.backupFileName))
    val defaultConfigResource = Resource.make(defaultConfigFileSource)(src => IO(src.close()))
    for {
      mainConfigString <- mainConfigResource.use(src => IO.blocking(src.getLines().mkString("\n")))
      mainConfigSource = ConfigSource.string(mainConfigString)

      defaultConfigString <- defaultConfigResource.use(src => IO.blocking(src.getLines().mkString("\n")))
      defaultConfigSource = ConfigSource.string(defaultConfigString)

      config = mainConfigSource.withFallback(defaultConfigSource).loadOrThrow[Conf]
    } yield (config)
  }
}