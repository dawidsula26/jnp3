package stockrabbit.statistics.environment.kafka

import cats.effect._
import scala.annotation.unused
import fs2.kafka.KafkaConsumer
import fs2.kafka.ConsumerSettings
import fs2.kafka.ProducerSettings
import fs2.kafka._
import fs2.Stream
import stockrabbit.statistics.model.Variable
import org.apache.kafka.common.{serialization => ser}
import nequi.circe.kafka._

trait EnvKafka[F[_]] {
  def inputTopic: KafkaConsumer[F, String, String]
  def backfeedTopic: Stream[F, (String, String)] => Stream[F, ProducerResult[Unit, String, String]]
}

object EnvKafka {
  def impl[F[_]: Async](@unused config: ConfigKafka): Resource[F, EnvKafka[F]] = {
    new EnvKafkaBuilder(config).build
  }
}

private class EnvKafkaBuilder[F[_]: Async](config: ConfigKafka) {
  def makeConsumerTopic[K, V](topic: Topic)(implicit a: Deserializer[F, K], b: Deserializer[F, V]): Resource[F, KafkaConsumer[F, K, V]] = {
    val settings = ConsumerSettings[F, K, V]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(config.address.str)
      .withGroupId(topic.consumerGroup)
    KafkaConsumer.resource(settings)
      .evalTap(_.subscribeTo(topic.name))
  }

  def makeProducerTopic[K, V](topic: Topic)(implicit a: Serializer[F, K], b: Serializer[F, V]) = {
    def valueWrapper(t: (K, V)): ProducerRecords[Unit, K, V] = {
      val record = ProducerRecord(topic.name, t._1, t._2) 
      ProducerRecords.one(record)
    }
    
    val settings = ProducerSettings[F, K, V]
      .withBootstrapServers(config.address.str)
    val producerPipe = KafkaProducer.pipe[F, K, V, Unit](settings)
    print(producerPipe)

    def processStream(stream: Stream[F, (K, V)]): Stream[F, ProducerResult[Unit, K, V]] = {
      val recordStream = stream.map(valueWrapper(_))
      val producerStream = recordStream.through(producerPipe)
      producerStream
    }
    stream => processStream(stream)
  }

  def build: Resource[F, EnvKafka[F]] = {
    implicit val variableSerializer = Serializer.delegate(implicitly[ser.Serializer[Variable]])
    implicit val variableDeserializer = Deserializer.delegate(implicitly[ser.Deserializer[Variable]])
    println(variableSerializer)
    println(variableDeserializer)

    for {
      input <- makeConsumerTopic[String, String](config.inputTopic)
      backfeed = makeProducerTopic[String, String](config.backfeedTopic)
    } yield (new EnvKafka[F] {
      def inputTopic = input
      def backfeedTopic = backfeed
    })
  }
}