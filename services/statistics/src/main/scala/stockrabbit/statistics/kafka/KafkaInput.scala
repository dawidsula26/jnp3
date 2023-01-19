package stockrabbit.statistics.kafka

import fs2.kafka.CommittableConsumerRecord
import cats.effect._
import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.model.Variable
import stockrabbit.statistics.mongo.VariableCollection
import scala.annotation.unused
import io.circe.parser._

class KafkaInput(env: Environment[IO]) {
  def processRecord(
    collection: VariableCollection[IO], 
    record: CommittableConsumerRecord[IO, String, String]
  ): IO[Unit] = for {
    variableJson = parse(record.record.value)
    variableParsed = variableJson.toOption.get.as[Variable]
    variable = variableParsed.toOption.get
    _ <- IO.blocking(println(variable))
    _ <- collection.insert(Seq(variable))
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