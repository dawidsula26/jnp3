package stockrabbit.common.environment.kafka

import stockrabbit.common.environment.general.Address

trait ConfigKafka{
  def address: Address
  def consumerGroup: String
}

case class InputTopic(name: String)

case class OutputTopic(name: String)
