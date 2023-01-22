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
import org.apache.kafka.common.serialization.Serde


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
  private def serialize[T: Encoder](topic: String, data: T): Array[Byte] =
    Serdes.stringSerde.serializer().serialize(topic, data.asJson.noSpaces)

  private def deserialize[T: Decoder](topic: String, data: Array[Byte]): Option[T] = {
    val string = Serdes.stringSerde.deserializer().deserialize(topic, data)
    for {
      json <- parse(string).toOption
      value <- json.as[T].toOption
    } yield (value)
  }

  def serde[T >: Null: Encoder: Decoder]: Serde[T] = 
    Serdes.fromFn[T](serialize[T](_, _), deserialize[T](_, _))

  def consumerTopic[V >: Null: Encoder: Decoder](topic: InputTopic, builder: StreamsBuilder): KStream[String, V] = {
    builder.stream(topic.name)(Consumed.`with`(Serdes.stringSerde, serde[V]))
  }

  def producerTopic[V >: Null: Decoder: Encoder](topic: OutputTopic)(stream: KStream[String, V]): Unit = {
    stream.to(topic.name)(Produced.`with`(Serdes.stringSerde, serde[V]))
  }

  implicit class StreamWithTo[K, V](stream: KStream[K, V]) {
    def to(topic: KStream[K, V] => Unit) = topic(stream)
  }
}
