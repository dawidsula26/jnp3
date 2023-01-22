package stockrabbit.statistics.environment.kafka

import stockrabbit.common.environment.{kafka => common}
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala._
import stockrabbit.common.environment.general.Address
import stockrabbit.common.model.variable._

trait EnvKafka extends common.EnvKafka {
  def processedTopic(builder: StreamsBuilder): KStream[Name, Variable]
  def backfeedTopic(stream: KStream[Name, Variable]): Unit
}

object EnvKafka {
  def impl(config: ConfigKafka): EnvKafka = {
    new EnvKafka {
      protected def address: Address = config.address

      protected def consumerGroup: String = config.consumerGroup

      def processedTopic(builder: StreamsBuilder): KStream[Name,Variable] = 
        common.EnvKafka.consumerTopic(config.processedTopic, builder)

      def backfeedTopic(stream: KStream[Name, Variable]): Unit =
        common.EnvKafka.producerTopic[Name, Variable](config.backfeedTopic)(stream)
    }
  }
}
