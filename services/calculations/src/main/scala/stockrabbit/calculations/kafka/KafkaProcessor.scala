package stockrabbit.calculations.kafka

import cats.effect._
import stockrabbit.calculations.environment.general.Environment
import stockrabbit.common.environment.{kafka => common}
import stockrabbit.common.environment.kafka.EnvKafka.StreamWithTo
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.Grouped
import java.time.Instant
import scala.annotation.unused
import org.apache.kafka.streams.scala.kstream.Materialized
import stockrabbit.common.model.variable._
import stockrabbit.common.model.variable.name._

class KafkaProcessor(env: Environment[IO]) {
  def initAggregation: Variable =
    Variable(
      Name(Statistic("agg"), Some(Strategy("strat")), Some(Subject("subj"))),
      Value(0),
      Timestamp(Instant.ofEpochSecond(0))
    )

  def processAggregation(@unused key: Name, v: Variable, agg: Variable): Variable =
    Variable(
      v.name,
      Value(v.value.value + agg.value.value),
      v.time
    )

  def build(builder: StreamsBuilder): IO[Unit] = {
    implicit val grouped = 
      Grouped.`with`(common.EnvKafka.serde[Name], common.EnvKafka.serde[Variable])
      
    implicit val materialized: Materialized[Name, Variable, ByteArrayKeyValueStore] = 
      Materialized
        .`with`(common.EnvKafka.serde[Name], common.EnvKafka.serde[Variable])
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