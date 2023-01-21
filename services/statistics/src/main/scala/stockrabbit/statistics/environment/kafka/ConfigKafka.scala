package stockrabbit.statistics.environment.kafka

import stockrabbit.statistics.environment.general.Address

case class ConfigKafka(
  address: Address,

  processedTopic: Topic,
  backfeedTopic: Topic
)

case class Topic (
  name: String,
  consumerGroup: String
)
