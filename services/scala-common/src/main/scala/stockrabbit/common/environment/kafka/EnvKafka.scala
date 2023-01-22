package stockrabbit.common.environment.kafka

import cats.effect._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.streams.scala._
import stockrabbit.common.environment.general.Address
import org.apache.kafka.streams.KafkaStreams
import java.util.Properties
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.serialization.Serdes


trait EnvKafka {
  protected def address: Address
  protected def consumerGroup: String
  
  private def kafkaProperties: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, consumerGroup)
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, address.str)
    p
  }

  def runStreams(makeStreams: StreamsBuilder => IO[Unit]): Resource[IO, KafkaStreams] = {
    val builder = new StreamsBuilder
    for {
      _ <- Resource.eval(makeStreams(builder))
      streams <- Resource.eval(IO(new KafkaStreams(builder.build(), kafkaProperties)))
      streamsResource <- Resource.make(IO(streams.start()).as(streams))(streams => IO(streams.close()))
    } yield (streamsResource)
  }
}

object EnvKafka {
  private def consumerTopicImpl(topic: InputTopic, builder: StreamsBuilder): KStream[String, String] = {
    builder.stream(topic.name)(Consumed.`with`(Serdes.stringSerde, Serdes.stringSerde))
  }

  def consumerTopic[V: Decoder](topic: InputTopic, builder: StreamsBuilder): KStream[String, V] = {
    consumerTopicImpl(topic, builder)
      .flatMapValues(v => parse(v).toOption)
      .flatMapValues(v => v.as[V].toOption)
  }

  private def producerTopicImpl(topic: OutputTopic)(stream: KStream[String, String]): Unit = {
    stream.to(topic.name)(Produced.`with`(Serdes.stringSerde, Serdes.stringSerde))
  }

  def producerTopic[V: Encoder](topic: OutputTopic)(stream: KStream[String, V]): Unit = {
    stream
      .mapValues(_.asJson)
      .mapValues(_.noSpaces)
      .to(producerTopicImpl(topic)(_))
  }

  implicit class StreamWithTo[K, V](stream: KStream[K, V]) {
    def to(topic: KStream[K, V] => Unit) = topic(stream)
  }
}
