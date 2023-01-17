package stockrabbit.statistics.environment.kafka

import stockrabbit.statistics.environment.general.Address

case class ConfigKafka(
  address: Address,
  inputTopic: String,
  backfeedTopic: String
)
