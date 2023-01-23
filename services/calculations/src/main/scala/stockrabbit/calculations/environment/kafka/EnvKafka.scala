package stockrabbit.calculations.environment.kafka

import stockrabbit.common.environment.{kafka => common}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import stockrabbit.common.environment.general.Address
import stockrabbit.common.model.variable._

trait EnvKafka extends common.EnvKafka {
  def processedTopic(stream: KStream[Name, Variable]): Unit
  def inputTopic(builder: StreamsBuilder): KStream[Name, Variable]
  def backfeedTopic(builder: StreamsBuilder): KStream[Name, Variable]
}

object EnvKafka {
  def impl(config: ConfigKafka): EnvKafka = {
    new EnvKafka {
      protected def address: Address = config.address

      protected def consumerGroup: String = config.consumerGroup

      def processedTopic(stream: KStream[Name,Variable]): Unit = 
        common.EnvKafka.producerTopic[Name, Variable](config.processedTopic)(stream)

      def inputTopic(builder: StreamsBuilder): KStream[Name,Variable] = 
        common.EnvKafka.consumerTopic(config.inputTopic, builder)

      def backfeedTopic(builder: StreamsBuilder): KStream[Name,Variable] =
        common.EnvKafka.consumerTopic(config.backfeedTopic, builder)
    }
  }
}
