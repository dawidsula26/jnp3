package stockrabbit.calculations.kafka

import cats.effect._
import stockrabbit.calculations.environment.general.Environment
import stockrabbit.common.model.Variable

class KafkaInput(env: Environment[IO]) {
  def processRecord(
    variable: Variable
  ): IO[Variable] = for {
    _ <- IO.blocking(println(variable))
  } yield (variable)

  def run(): IO[Unit] = for {
    _ <- env.kafka.inputTopic
      .evalMap(processRecord(_))
      .through(env.kafka.processedTopic)
      .compile.drain
  } yield ()
}

object KafkaInput {
  def run(env: Environment[IO]): IO[Unit] = {
    val runner = new KafkaInput(env)
    runner.run()
  }
}