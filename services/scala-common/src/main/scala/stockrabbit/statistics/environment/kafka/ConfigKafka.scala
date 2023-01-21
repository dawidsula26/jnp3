package stockrabbit.statistics.environment.kafka

import stockrabbit.statistics.environment.general.Address

trait ConfigKafka{
  def address: Address
}

trait Topic {
  def name: String
  def consumerGroup: String
}
