package stockrabbit.statistics.environment.kafka

import cats.effect._
import scala.annotation.unused


trait EnvKafka[F[_]] {
  def kafka: String
}

object EnvKafka {
  def impl[F[_]](@unused config: ConfigKafka): Resource[F, EnvKafka[F]] = {
    Resource.pure(new EnvKafka[F] {
      def kafka = "KAFKA"
    })
  }
}
