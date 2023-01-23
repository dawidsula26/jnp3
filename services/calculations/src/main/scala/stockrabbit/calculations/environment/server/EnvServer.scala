package stockrabbit.calculations.environment.server

import cats.effect._

trait EnvServer[F[_]] {
  def port: Int
}

object EnvServer {
  def impl[F[_]](config: ConfigServer): Resource[F, EnvServer[F]] = {
    Resource.pure(new EnvServer[F] {
      def port = config.port
    })
  }
}
