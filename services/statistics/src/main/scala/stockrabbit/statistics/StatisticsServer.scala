package stockrabbit.statistics

import cats.effect.Async
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object StatisticsServer {

  def run[F[_]: Async]: F[Nothing] = {
    val readerAlg = reader.Reader.impl[F]
    val httpApp = (
      reader.Routes.routes[F](readerAlg)
    ).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
