package stockrabbit.calculations.kafka

import cats.effect._
import stockrabbit.calculations.environment.general.Environment
import stockrabbit.calculations.kafka.Inputs
import stockrabbit.common.environment.kafka.EnvKafka.StreamWithTo
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala._
import stockrabbit.common.model.variable._
import stockrabbit.calculations.kafka.logic.Recent
import java.time.Duration
import stockrabbit.common.model.variable.name._

class Process(env: Environment[IO]) {
  def initAggregation: Option[Value] = None

  def processAggregation(s: Option[Value], i: Variable): (Option[Value], Variable) = {
    val v = s.map(ss => Value(ss.value + i.value.value)).getOrElse(i.value)
    val o = i.copy(value = v)
    (Some(v), o)
  }

  def build(builder: StreamsBuilder): IO[Unit] = {
    val input = env.kafka.inputTopic(builder)
    val inputs = Inputs.impl(input)

    inputs.stockValues.to(env.kafka.processedTopic _)
    inputs.stockTrades.to(env.kafka.processedTopic _)

    Recent.sum(Statistic("stockValueSum"), Duration.ofHours(1))(inputs.stockValues)
      .to(env.kafka.processedTopic _)
    
    Recent.avg(Statistic("stockValueAvg"), Duration.ofHours(1))(inputs.stockValues)
      .to(env.kafka.processedTopic _)

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