package stockrabbit.statistics.manager

import stockrabbit.statistics.environment.general.Environment

import com.mongodb.client.model.TimeSeriesOptions
import mongo4cats.models.database.CreateCollectionOptions
import cats._
import cats.implicits._
import mongo4cats.bson.Document
import mongo4cats.bson.BsonValue
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

      val var1 = Document(
        Variable.Schema.name -> BsonValue.string("v"),
        Variable.Schema.value -> BsonValue.double(4.8),
        Variable.Schema.time -> BsonValue.instant(Instant.ofEpochSecond(0))
      )

      for {
        collection <- database.getCollection(env.mongo.collectionName)
        _ <- collection.insertOne(var1)
      } yield ()
    }
  }
}