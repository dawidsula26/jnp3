package stockrabbit.statistics

import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.environment.general.Config
import stockrabbit.statistics.manager.Manager
import stockrabbit.statistics.reader.Reader

import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import pureconfig._
import pureconfig.generic.auto._

object StatisticsServer {

  def run[F[_]: Async]: F[Nothing] = {
    val config = ConfigSource.default.loadOrThrow[Config]
    val resources = for {
      env <- Environment.impl(config)

      readerAlg = Reader.impl[F](env)
      managerAlg = Manager.impl[F](env)

      httpApp = (
        reader.Routes.routes[F](readerAlg) <+> 
        manager.Routes.routes[F](managerAlg)
      ).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      
      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
    resources.useForever
  }
}
