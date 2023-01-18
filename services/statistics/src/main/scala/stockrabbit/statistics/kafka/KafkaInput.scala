package stockrabbit.statistics.kafka

import fs2.kafka.CommittableConsumerRecord
import cats.effect._
import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.model.Variable
import stockrabbit.statistics.mongo.VariableCollection

class KafkaInput(env: Environment[IO]) {
  def processRecord(
    collection: VariableCollection[IO], 
    record: CommittableConsumerRecord[IO, String, Variable]
  ): IO[Unit] = for {
    _ <- IO.blocking(println(record.record.key))
    _ <- IO.blocking(println(record.record.value))
    _ <- collection.insert(Seq(record.record.value))
  } yield ()

  def run(): IO[Unit] = for {
    variableCollection <- VariableCollection.get(env.mongo.database)
    _ <- env.kafka.inputTopic
      .records
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