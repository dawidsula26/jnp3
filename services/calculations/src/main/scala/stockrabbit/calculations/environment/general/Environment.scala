package stockrabbit.calculations.environment.general

import stockrabbit.calculations.environment.kafka.EnvKafka
import stockrabbit.calculations.environment.server.EnvServer

trait Environment[F[_]] {
  def kafka: EnvKafka
  def server: EnvServer[F]
}

object Environment {
  def impl[F[_]](config: Config) = {
    for {
      envServer <- EnvServer.impl[F](config.server)
      envKafka = EnvKafka.impl(config.kafka)
    } yield (new Environment[F]{
      def kafka = envKafka
      def server = envServer
    })
  }
}
