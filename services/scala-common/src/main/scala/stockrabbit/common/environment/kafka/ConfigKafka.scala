package stockrabbit.common.environment.kafka

import stockrabbit.common.environment.general.Address

trait ConfigKafka{
  def address: Address
}

case class InputTopic(
  name: String,
  consumerGroup: String
)

case class OutputTopic(
  name: String
)
