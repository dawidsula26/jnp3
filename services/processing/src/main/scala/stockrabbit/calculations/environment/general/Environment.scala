package stockrabbit.calculations.environment.general

import stockrabbit.calculations.environment.kafka.EnvKafka
import stockrabbit.calculations.environment.server.EnvServer

import cats.effect._

trait Environment[F[_]] {
  def kafka: EnvKafka[F]
  def server: EnvServer[F]
}

object Environment {
  def impl[F[_]: Async](config: Config) = {
    for {
      envKafka <- EnvKafka.impl[F](config.kafka)
      envServer <- EnvServer.impl[F](config.server)
    } yield (new Environment[F]{
      def kafka = envKafka
      def server = envServer
    })
  }
}
