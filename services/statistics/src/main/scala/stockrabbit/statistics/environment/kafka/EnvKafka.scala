package stockrabbit.statistics.environment.kafka

import cats.effect._
import fs2.kafka._
import fs2.Stream
import stockrabbit.statistics.model.Variable
import stockrabbit.common.environment.{kafka => common}

trait EnvKafka[F[_]] {
  def processedTopic: Stream[F, Variable]
  def backfeedTopic: Stream[F, Variable] => Stream[F, ProducerResult[Unit, String, String]]
}

object EnvKafka {
  def impl[F[_]: Async](config: ConfigKafka): Resource[F, EnvKafka[F]] = {
    new EnvKafkaBuilder(config).build
  }
}

private class EnvKafkaBuilder[F[_]: Async](config: ConfigKafka) {
  def makeProcessedTopic: Resource[F, Stream[F, Variable]] =
    common.EnvKafka.consumerTopic[F, Variable](config, config.processedTopic)

  def makeBackfeedTopic: Stream[F, Variable] => Stream[F, ProducerResult[Unit, String, String]] =
    common.EnvKafka.producerTopic(config, config.backfeedTopic, _.name)

  def build: Resource[F, EnvKafka[F]] = {
    for {
      input <- makeProcessedTopic
      backfeed = makeBackfeedTopic
    } yield (new EnvKafka[F] {
      def processedTopic = input
      def backfeedTopic = backfeed
    })
  }
}