package stockrabbit.statistics.manager

import stockrabbit.statistics.environment.general.Environment

import com.mongodb.client.model.TimeSeriesOptions
import mongo4cats.models.database.CreateCollectionOptions
import cats._
import cats.implicits._
import java.time.Instant
import stockrabbit.statistics.model.Variable

trait Manager[F[_]] {
  def initializeDatabase(): F[Unit]
  def removeDatabase(): F[Unit]
  def fillWithGarbage(): F[Unit]
}

object Manager {
  def impl[F[_]: Monad](env: Environment[F]): Manager[F] = new Manager[F] {
    def initializeDatabase(): F[Unit] = {
      val optionsTimeseries = 
        new TimeSeriesOptions(Variable.Schema.time)
        .metaField(Variable.Schema.name)
      val optionsCollection = CreateCollectionOptions().timeSeriesOptions(optionsTimeseries)
      val database = env.mongo.database
      database.createCollection(env.mongo.collectionName, optionsCollection)
    }

    def removeDatabase(): F[Unit] = {
      val database = env.mongo.database
      database.drop
    }

    def fillWithGarbage(): F[Unit] = {
      val database = env.mongo.database

      def v(v: Double, t: Long): Variable = Variable(
        Variable.Name("v"), 
        Variable.Value(v), 
        Variable.Timestamp(Instant.ofEpochSecond(t))
      )
      
      for {
        collection <- database.getCollection(env.mongo.collectionName)
        _ <- collection.insertMany(Seq(
          v(5.8, 0),
          v(4.7, 1),
          v(8.0, 2),
          v(6.5, 3),
          v(5.9, 4)
        ).map(_.document))
      } yield ()
    }
  }
}