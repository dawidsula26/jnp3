package stockrabbit.statistics.kafka

import cats.effect._
import cats.effect.unsafe.{implicits => unsafe}
import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.mongo.VariableCollection
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.KafkaStreams
import stockrabbit.common.model.variable.Variable

class KafkaInput(env: Environment[IO]) {
  def processRecord(
    collection: VariableCollection[IO], 
    variable: Variable
  ): Unit = {
    val io = for {
      _ <- collection.insert(Seq(variable))
    } yield ()
    io.unsafeRunSync()(unsafe.global)
  }

  private def build(collection: VariableCollection[IO], builder: StreamsBuilder): IO[Unit] = {
    val a = env.kafka.processedTopic(builder)
      .foreach{(_, v) => processRecord(collection, v)}
    
    print(a)
    IO.pure(())
  }

  def run(): Resource[IO, KafkaStreams] = {
    for {
      collection <- Resource.eval(VariableCollection.get(env.mongo.database))
      streams <- env.kafka.runStreams(build(collection, _))
    } yield (streams)
  }
}

object KafkaInput {
  def run(env: Environment[IO]): Resource[IO, KafkaStreams] = {
    val runner = new KafkaInput(env)
    runner.run()
  }
}