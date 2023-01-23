package stockrabbit.calculations.manager

import stockrabbit.calculations.environment.general.Environment

import cats._
import cats.implicits._
import scala.annotation.unused

trait Manager[F[_]] {
  def ping: F[String]
}

object Manager {
  def impl[F[_]: Applicative](@unused env: Environment[F]): Manager[F] = new Manager[F] {
    def ping = "pong".pure(implicitly[Applicative[F]])
  }
}