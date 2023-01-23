package stockrabbit.statistics.manager

import stockrabbit.statistics.environment.general.Environment

import cats._
import cats.implicits._
import java.time.Instant
import stockrabbit.statistics.mongo.VariableCollection
import stockrabbit.common.model.variable._
import stockrabbit.common.model.variable.name._

trait Manager[F[_]] {
  def initializeDatabase(): F[Unit]
  def removeDatabase(): F[Unit]
  def fillWithGarbage(): F[Unit]
}

object Manager {
  def impl[F[_]: Monad](env: Environment[F]): Manager[F] = new Manager[F] {
    def initializeDatabase(): F[Unit] =
      VariableCollection.initialize(env.mongo.database)

    def removeDatabase(): F[Unit] =
      env.mongo.database.drop

    def fillWithGarbage(): F[Unit] = {
      val database = env.mongo.database

      def v(v: Double, t: Long): Variable = Variable(
        Name(Statistic("stat"), Some(Strategy("strat")), Some(Subject("subj"))), 
        Value(v), 
        Timestamp(Instant.ofEpochSecond(t))
      )
      val toInsert = Seq(
        v(5.8, 0),
        v(4.7, 1),
        v(8.0, 2),
        v(6.5, 3),
        v(5.9, 4)
      )

      for {
        collection <- VariableCollection.get(database)
        _ <- collection.insert(toInsert)
      } yield ()
    }
  }
}