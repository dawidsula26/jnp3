package stockrabbit.statistics.environment.general

import stockrabbit.statistics.environment.kafka.ConfigKafka
import stockrabbit.statistics.environment.mongo.ConfigMongo
import stockrabbit.statistics.environment.server.ConfigServer

case class Config(
  kafka: ConfigKafka,
  mongo: ConfigMongo,
  server: ConfigServer
)
