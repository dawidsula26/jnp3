package stockrabbit.calculations.kafka

import cats.effect._
import cats.effect.unsafe.{implicits => unsafe}
import stockrabbit.calculations.environment.general.Environment
import stockrabbit.common.model.Variable
import stockrabbit.common.environment.kafka.EnvKafka.StreamWithTo
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala._

class KafkaProcessor(env: Environment[IO]) {
  def processRecord(variable: Variable): Variable = {
    val io = for {
      _ <- IO.blocking(println(variable))
      _ <- IO.blocking(println("Test"))
    } yield (variable)
    io.unsafeRunSync()(unsafe.global)
  }

  def build(builder: StreamsBuilder): IO[Unit] = {
    val a = env.kafka.inputTopic(builder)
      .mapValues{processRecord(_)}
      .to(env.kafka.processedTopic(_))
    
    env.kafka.inputTopic(builder)
      .pro

    print(a)
    IO.pure(())
  }

  def run(): Resource[IO, KafkaStreams] = {
    env.kafka.runStreams(build(_))
  }
}

object KafkaProcessor {
  def run(env: Environment[IO]): Resource[IO, KafkaStreams] = {
    val runner = new KafkaProcessor(env)
    runner.run()
  }
}