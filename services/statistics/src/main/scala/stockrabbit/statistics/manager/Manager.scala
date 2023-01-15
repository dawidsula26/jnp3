package stockrabbit.statistics.manager

import stockrabbit.statistics.environment.general.Environment

import com.mongodb.client.model.TimeSeriesOptions
import mongo4cats.models.database.CreateCollectionOptions
import cats._
import cats.implicits._
import stockrabbit.statistics.model.Variable
import mongo4cats.circe._

trait Manager[F[_]] {
  def initializeDatabase(): F[Unit]
  def removeDatabase(): F[Unit]
  def fillWithGarbage(): F[Unit]
}

object Manager {
  def impl[F[_]: Monad](env: Environment[F]): Manager[F] = new Manager[F] {
    def initializeDatabase(): F[Unit] = {
      val optionsTimeseries = new TimeSeriesOptions("time").metaField("variable")
      val optionsCollection = CreateCollectionOptions().timeSeriesOptions(optionsTimeseries)
      val database = env.mongo.database
      database.createCollection("variables", optionsCollection)
    }

    def removeDatabase(): F[Unit] = {
      val database = env.mongo.database
      database.drop
    }

    def fillWithGarbage(): F[Unit] = {
      val database = env.mongo.database

      val var1 = Variable(Variable.Name("v"), Variable.Value(5), Variable.Timestamp(0))
      val var2 = Variable(Variable.Name("v"), Variable.Value(9), Variable.Timestamp(1))
      val var3 = Variable(Variable.Name("v"), Variable.Value(3), Variable.Timestamp(2))
      val var4 = Variable(Variable.Name("t"), Variable.Value(8), Variable.Timestamp(3))

      for {
        collection <- database.getCollectionWithCodec[Variable]("variables")
        _ <- collection.insertMany(Seq(
          var1,
          var2,
          var3,
          var4
        ))
      } yield ()
    }
  }
}