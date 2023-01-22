package stockrabbit.calculations.kafka

import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.ByteArrayKeyValueStore
import stockrabbit.common.environment.kafka.EnvKafka.serde
import io.circe._
import io.circe.generic.auto._
import org.apache.kafka.streams.kstream.JoinWindows
import java.time.Duration

object Transform {
  def mono[
    S: Encoder: Decoder, 
    K >: Null: Encoder: Decoder, 
    I >: Null: Encoder: Decoder, 
    O: Encoder: Decoder
  ](init: => S, process: (S, I) => (S, O))(stream: KStream[K, I]): KStream[K, O] = {
    implicit val groupBy = Grouped.`with`(serde[K], serde[I])
    implicit val materialization: Materialized[K, Either[S, (S, O)], ByteArrayKeyValueStore] = Materialized
      .`with`(serde[K], serde[Either[S, (S, O)]])
      .withCachingDisabled()
    stream
      .groupByKey
      .aggregate[Either[S, (S, O)]](Left(init)){ 
        case (_, i, Left(s)) => Right(process(s, i))
        case (_, i, Right((s, _))) => Right(process(s, i))
      }(materialization)
      .toStream
      .mapValues[O]((v: Either[S, (S, O)]) => v match {
        case Left(_) => throw new AssertionError("Should only return right")
        case Right((_, o)) => o
      })
  }

  def bi[
    S: Encoder: Decoder,
    K >: Null: Encoder: Decoder,
    I1 >: Null: Encoder: Decoder,
    I2 >: Null: Encoder: Decoder,
    O: Encoder: Decoder
  ](init: => S, process: (S, I1, I2) => (S, O))(stream1: KStream[K, I1], stream2: KStream[K, I2]): KStream[K, O] = {
    implicit val streamJoin = StreamJoined.`with`(serde[K], serde[I1], serde[I2])
    val stream = stream1.join(stream2)((_, _), JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)))
    def processPair(s: S, v: (I1, I2)): (S, O) = {
      val (i1, i2) = v
      process(s, i1, i2)
    }
    mono[S, K, (I1, I2), O](init, processPair)(stream)
  }
}