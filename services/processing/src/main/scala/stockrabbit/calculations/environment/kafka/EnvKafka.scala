package stockrabbit.calculations.environment.kafka

import cats.effect._
import fs2.kafka._
import fs2.Stream
import stockrabbit.common.model.Variable
import stockrabbit.common.environment.{kafka => common}

trait EnvKafka[F[_]] {
  def processedTopic: Stream[F, Variable] => Stream[F, ProducerResult[Unit, String, String]]
  def inputTopic: Stream[F, Variable]
  def backfeedTopic: Stream[F, Variable]
}

object EnvKafka {
  def impl[F[_]: Async](config: ConfigKafka): Resource[F, EnvKafka[F]] = {
    new EnvKafkaBuilder(config).build
  }
}

private class EnvKafkaBuilder[F[_]: Async](config: ConfigKafka) {
  def makeProcessedTopic: Stream[F, Variable] => Stream[F, ProducerResult[Unit, String, String]] =
    common.EnvKafka.producerTopic[F, Variable.Name, Variable](config, config.processedTopic, _.name)

  def makeInputTopic: Resource[F, Stream[F, Variable]] =
    common.EnvKafka.consumerTopic[F, Variable](config, config.inputTopic)

  def makeBackfeedTopic: Resource[F, Stream[F, Variable]] =
    common.EnvKafka.consumerTopic[F, Variable](config, config.backfeedTopic)

  def build: Resource[F, EnvKafka[F]] = {
    for {
      backfeed <- makeBackfeedTopic
      input <- makeInputTopic
      processed = makeProcessedTopic
    } yield (new EnvKafka[F] {
      def processedTopic =  processed
      def backfeedTopic = backfeed
      def inputTopic = input
    })
  }
}