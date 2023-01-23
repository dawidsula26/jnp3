package stockrabbit.statistics.environment.general

import stockrabbit.statistics.environment.kafka.ConfigKafka
import stockrabbit.statistics.environment.mongo.ConfigMongo
import stockrabbit.statistics.environment.server.ConfigServer
import stockrabbit.common.environment.{general => common}
import stockrabbit.common.environment.general.SetupVersion
import pureconfig.generic.auto._

case class Config(
  kafka: ConfigKafka,
  mongo: ConfigMongo,
  server: ConfigServer
)

object Config {
  def impl(version: SetupVersion)= {
    common.Config.impl[Config](version)
  }
}
