package stockrabbit.calculations.kafka

import cats.effect._
import stockrabbit.calculations.environment.general.Environment
import stockrabbit.common.environment.kafka.EnvKafka.StreamWithTo
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala._
import stockrabbit.common.model.variable._

class Process(env: Environment[IO]) {
  def initAggregation: Option[Value] = None

  def processAggregation(s: Option[Value], i: Variable): (Option[Value], Variable) = {
    val v = s.map(ss => Value(ss.value + i.value.value)).getOrElse(i.value)
    val o = i.copy(value = v)
    (Some(v), o)
  }

  def build(builder: StreamsBuilder): IO[Unit] = {
    val input = env.kafka.inputTopic(builder)
    val sums = Transform.mono[Option[Value], Name, Variable, Variable](initAggregation, processAggregation(_, _))(input)
    sums.to(env.kafka.processedTopic(_))

    IO.pure(())
  }

  def run(): Resource[IO, KafkaStreams] = {
    env.kafka.runStreams(build(_))
  }
}

object Process {
  def run(env: Environment[IO]): Resource[IO, KafkaStreams] = {
    val runner = new Process(env)
    runner.run()
  }
}