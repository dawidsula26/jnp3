package stockrabbit.statistics.kafka

import fs2.Stream
import fs2.kafka.CommittableConsumerRecord
import cats.effect._
import stockrabbit.statistics.environment.general.Environment

class KafkaInput(env: Environment[IO]) {
  def processRecord(record: CommittableConsumerRecord[IO, String, String]): IO[Unit] = {
    IO.blocking(println(record.record.value))
  }

  def run(): IO[Unit] = {
    val values = Seq(("key", "value"), ("a", "b"), ("just", "strings"))
    Stream.iterable(values)
      .evalTap(v => IO.blocking(println(s"Sending ${v._1} -> ${v._2}")))
      .through(env.kafka.backfeedTopic)
      .compile.drain
  }
}

object KafkaInput {
  def run(env: Environment[IO]): IO[Unit] = {
    val runner = new KafkaInput(env)
    runner.run()
  }
}