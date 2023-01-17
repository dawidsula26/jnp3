package stockrabbit.statistics.environment.general

import stockrabbit.statistics.environment.kafka.ConfigKafka
import stockrabbit.statistics.environment.mongo.ConfigMongo

case class Config(
  kafka: ConfigKafka,
  mongo: ConfigMongo
)
