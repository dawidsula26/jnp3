package stockrabbit.calculations.environment.kafka

import stockrabbit.common.model.Variable
import stockrabbit.common.environment.{kafka => common}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import stockrabbit.common.environment.general.Address

trait EnvKafka extends common.EnvKafka {
  def processedTopic(stream: KStream[String, Variable]): Unit
  def inputTopic(builder: StreamsBuilder): KStream[String, Variable]
  def backfeedTopic(builder: StreamsBuilder): KStream[String, Variable]
}

object EnvKafka {
  def impl(config: ConfigKafka): EnvKafka = {
    new EnvKafka {
      protected def address: Address = config.address

      protected def consumerGroup: String = config.consumerGroup

      def processedTopic(stream: KStream[String,Variable]): Unit = 
        common.EnvKafka.producerTopic[Variable](config.processedTopic)(stream)

      def inputTopic(builder: StreamsBuilder): KStream[String,Variable] = 
        common.EnvKafka.consumerTopic(config.inputTopic, builder)

      def backfeedTopic(builder: StreamsBuilder): KStream[String,Variable] =
        common.EnvKafka.consumerTopic(config.backfeedTopic, builder)
    }
  }
}
