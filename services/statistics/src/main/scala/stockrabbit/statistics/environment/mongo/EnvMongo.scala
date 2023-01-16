package stockrabbit.statistics.environment.mongo

import mongo4cats.database.MongoDatabase
import mongo4cats.client.MongoClient
import mongo4cats.models.client.ServerAddress
import cats.effect._

trait EnvMongo[F[_]] {
  def collectionName: String

  def database: MongoDatabase[F]
}

object EnvMongo {
  def impl[F[_]: Async](config: ConfigMongo): Resource[F, EnvMongo[F]] = {
    val mongoAddress = ServerAddress(config.address.host, config.address.port)
    val mongoDatabase = MongoClient
      .fromServerAddress(mongoAddress)
      .evalMap(_.getDatabase(config.database))

    for {
      databaseValue <- mongoDatabase
    } yield (new EnvMongo[F] {
      def collectionName: String = "variables"
      def database: MongoDatabase[F] = databaseValue
    })
  }
}
