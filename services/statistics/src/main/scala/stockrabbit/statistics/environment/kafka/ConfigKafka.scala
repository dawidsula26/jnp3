package stockrabbit.statistics.environment.kafka

import stockrabbit.common.environment.general.Address
import stockrabbit.common.environment.kafka.InputTopic
import stockrabbit.common.environment.kafka.OutputTopic
import stockrabbit.common.environment.{kafka => common}

case class ConfigKafka(
  address: Address,
  consumerGroup: String,

  processedTopic: InputTopic,
  backfeedTopic: OutputTopic
) extends common.ConfigKafka
