package stockrabbit.statistics

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import mongo4cats.models.client._
import mongo4cats.client._

object StatisticsServer {

  def run[F[_]: Async]: F[Nothing] = {
    for {
      mongoClient <- MongoClient.fromServerAddress(ServerAddress("localhost", 27017))
      readerAlg = reader.Reader.impl[F](mongoClient)
      httpApp = reader.Routes.routes[F](readerAlg).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
