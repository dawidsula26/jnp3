package stockrabbit.statistics.environment.kafka

import stockrabbit.statistics.model.Variable
import stockrabbit.common.environment.{kafka => common}
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala._
import stockrabbit.common.environment.general.Address

trait EnvKafka extends common.EnvKafka {
  def processedTopic(builder: StreamsBuilder): KStream[String, Variable]
  def backfeedTopic(stream: KStream[String, Variable]): Unit
}

object EnvKafka {
  def impl(config: ConfigKafka): EnvKafka = {
    new EnvKafka {
      protected def address: Address = config.address

      protected def consumerGroup: String = config.consumerGroup

      def processedTopic(builder: StreamsBuilder): KStream[String,Variable] = 
        common.EnvKafka.consumerTopic(config.processedTopic, builder)

      def backfeedTopic(stream: KStream[String, Variable]): Unit =
        common.EnvKafka.producerTopic(config.backfeedTopic)(stream)
    }
  }
}
