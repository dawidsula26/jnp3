package stockrabbit.statistics.kafka

import cats.effect._
import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.model.Variable
import stockrabbit.statistics.mongo.VariableCollection

class KafkaInput(env: Environment[IO]) {
  def processRecord(
    collection: VariableCollection[IO], 
    variable: Variable
  ): IO[Unit] = for {
    _ <- IO.blocking(println(variable))
    _ <- collection.insert(Seq(variable))
  } yield ()

  def run(): IO[Unit] = for {
    variableCollection <- VariableCollection.get(env.mongo.database)
    _ <- env.kafka.processedTopic
      .evalMap(processRecord(variableCollection, _))
      .compile.drain
  } yield ()
}

object KafkaInput {
  def run(env: Environment[IO]): IO[Unit] = {
    val runner = new KafkaInput(env)
    runner.run()
  }
}