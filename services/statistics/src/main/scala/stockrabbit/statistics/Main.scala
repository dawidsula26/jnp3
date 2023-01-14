package stockrabbit.statistics

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = StatisticsServer.run[IO]
}
