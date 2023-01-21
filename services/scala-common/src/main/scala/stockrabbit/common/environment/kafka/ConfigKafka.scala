package stockrabbit.common.environment.kafka

import stockrabbit.common.environment.general.Address

trait ConfigKafka{
  def address: Address
}

case class Topic(
  name: String,
  consumerGroup: String
)
