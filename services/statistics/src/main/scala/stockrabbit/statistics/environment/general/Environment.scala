package stockrabbit.statistics.environment.general

import stockrabbit.statistics.environment.kafka.EnvKafka
import stockrabbit.statistics.environment.mongo.EnvMongo
import stockrabbit.statistics.environment.server.EnvServer

import cats.effect._

trait Environment[F[_]] {
  def mongo: EnvMongo[F]
  def kafka: EnvKafka
  def server: EnvServer[F]
}

object Environment {
  def impl[F[_]: Async](config: Config) = {
    for {
      envMongo <- EnvMongo.impl[F](config.mongo)
      envServer <- EnvServer.impl[F](config.server)
      envKafka = EnvKafka.impl(config.kafka)
    } yield (new Environment[F]{
      def mongo = envMongo
      def kafka = envKafka
      def server = envServer
    })
  }
}
