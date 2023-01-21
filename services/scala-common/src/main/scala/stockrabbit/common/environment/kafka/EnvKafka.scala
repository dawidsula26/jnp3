package stockrabbit.common.environment.kafka

import cats.effect._
import fs2.kafka._
import fs2.Stream
import io.circe._
import io.circe.parser._
import io.circe.syntax._


object EnvKafka {
  private def consumerTopicImpl[F[_]: Async, K, V]
    (config: ConfigKafka, topic: InputTopic)
    (implicit a: Deserializer[F, K], b: Deserializer[F, V]): 
      Resource[F, KafkaConsumer[F, K, V]] = 
  {
    val settings = ConsumerSettings[F, K, V]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(config.address.str)
      .withGroupId(topic.consumerGroup)
    KafkaConsumer.resource(settings)
      .evalTap(_.subscribeTo(topic.name))
  }

  def consumerTopic[F[_]: Async, V: Decoder]
    (config: ConfigKafka, topic: InputTopic): 
      Resource[F, Stream[F, V]] = 
  {
    for {
      consumer <- consumerTopicImpl[F, Unit, String](config, topic)
      result = consumer.records
        .map(record => parse(record.record.value))
        .map(_.toOption.get)
        .map(_.as[V])
        .map(_.toOption.get)
    } yield (result)
  }


  private def producerTopicImpl[F[_]: Async, K, V]
    (config: ConfigKafka, topic: OutputTopic)
    (implicit a: Serializer[F, K], b: Serializer[F, V]):
      Stream[F, (K, V)] => Stream[F, ProducerResult[Unit, K, V]] = 
  {
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

  def producerTopic[F[_]: Async, K: Encoder, V: Encoder]
    (config: ConfigKafka, topic: OutputTopic, key: V => K)
    (stream: Stream[F, V]): 
      Stream[F, ProducerResult[Unit, String, String]] = 
  {
    val streamWithKeys = stream.map(v => (key(v), v))
    val stringStream = streamWithKeys.map(v => (v._1.asJson.noSpaces, v._2.asJson.noSpaces))
    stringStream.through(producerTopicImpl[F, String, String](config, topic))
  }
}
