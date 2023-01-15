package stockrabbit.statistics.manager

import stockrabbit.statistics.environment.general.Environment

import com.mongodb.client.model.TimeSeriesOptions
import mongo4cats.models.database.CreateCollectionOptions


trait Manager[F[_]] {
  def initializeDatabase(): F[Unit]
}

object Manager {
  def impl[F[_]](env: Environment[F]): Manager[F] = new Manager[F] {
    def initializeDatabase(): F[Unit] = {
      val optionsTimeseries = new TimeSeriesOptions("time").metaField("variable")
      val optionsCollection = CreateCollectionOptions().timeSeriesOptions(optionsTimeseries)
      val database = env.mongo.database
      database.createCollection("variables", optionsCollection)
    }
  }
}