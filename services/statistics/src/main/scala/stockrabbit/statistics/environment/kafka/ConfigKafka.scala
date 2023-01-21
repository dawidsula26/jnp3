package stockrabbit.statistics.environment.kafka

import stockrabbit.common.environment.general.Address
import stockrabbit.common.environment.kafka.Topic
import stockrabbit.common.environment.{kafka => common}

case class ConfigKafka(
  address: Address,

  processedTopic: Topic,
  backfeedTopic: Topic
) extends common.ConfigKafka
