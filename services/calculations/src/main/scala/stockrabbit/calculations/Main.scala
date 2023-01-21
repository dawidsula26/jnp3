package stockrabbit.calculations

import cats.effect.IOApp

object Main extends IOApp.Simple {
  val run = StatisticsServer.run
}
