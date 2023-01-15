package stockrabbit.statistics.environment.general

import stockrabbit.statistics.environment.kafka.EnvKafka
import stockrabbit.statistics.environment.mongo.EnvMongo

import cats.effect._

trait Environment[F[_]] {
  def mongo: EnvMongo[F]
  def kafka: EnvKafka[F]
}

object Environment {
  def impl[F[_]: Async](config: Config) = {
    for {
      envKafka <- EnvKafka.impl[F](config.kafka)
      envMongo <- EnvMongo.impl[F](config.mongo)
    } yield (new Environment[F]{
      def mongo: EnvMongo[F] = envMongo
      def kafka: EnvKafka[F] = envKafka
    })
  }
}
