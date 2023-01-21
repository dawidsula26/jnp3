package stockrabbit.calculations.environment.general

import stockrabbit.calculations.environment.kafka.ConfigKafka
import stockrabbit.calculations.environment.server.ConfigServer
import stockrabbit.common.environment.{general => common}
import stockrabbit.common.environment.general.SetupVersion
import pureconfig.generic.auto._

case class Config(
  kafka: ConfigKafka,
  server: ConfigServer
)

object Config {
  def impl(version: SetupVersion)= {
    common.Config.impl[Config](version)
  }
}
