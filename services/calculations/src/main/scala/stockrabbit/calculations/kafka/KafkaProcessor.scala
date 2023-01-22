package stockrabbit.calculations.kafka

import cats.effect._
import stockrabbit.calculations.environment.general.Environment
import stockrabbit.common.model.Variable
import stockrabbit.common.environment.{kafka => common}
import stockrabbit.common.environment.kafka.EnvKafka.StreamWithTo
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.Grouped
import org.apache.kafka.streams.scala.serialization.Serdes
import java.time.Instant
import scala.annotation.unused
import org.apache.kafka.streams.scala.kstream.Materialized

class KafkaProcessor(env: Environment[IO]) {
  def initAggregation: Variable =
    Variable(
      Variable.Name("aggregation"),
      Variable.Value(0),
      Variable.Timestamp(Instant.ofEpochSecond(0))
    )

  def processAggregation(@unused topic: String, v: Variable, agg: Variable): Variable =
    Variable(
      v.name,
      Variable.Value(v.value.value + agg.value.value),
      v.time
    )

  def build(builder: StreamsBuilder): IO[Unit] = {
    implicit val grouped = 
      Grouped.`with`(Serdes.stringSerde, common.EnvKafka.serde[Variable])
      
    implicit val materialized: Materialized[String, Variable, ByteArrayKeyValueStore] = 
      Materialized
        .`with`(Serdes.stringSerde, common.EnvKafka.serde[Variable])
        .withCachingDisabled()

    env.kafka.inputTopic(builder)
      .groupByKey(grouped)
      .aggregate(initAggregation)(processAggregation)(materialized)
      .toStream
      .to(env.kafka.processedTopic(_))

    IO.pure(())
  }

  def run(): Resource[IO, KafkaStreams] = {
    env.kafka.runStreams(build(_))
  }
}

object KafkaProcessor {
  def run(env: Environment[IO]): Resource[IO, KafkaStreams] = {
    val runner = new KafkaProcessor(env)
    runner.run()
  }
}