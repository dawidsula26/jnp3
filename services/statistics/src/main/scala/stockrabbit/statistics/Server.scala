package stockrabbit.statistics

import stockrabbit.statistics.environment.general.Environment
import stockrabbit.statistics.environment.general.Config
import stockrabbit.statistics.reader.Reader

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server
import stockrabbit.statistics.kafka.KafkaInput
import stockrabbit.common.environment.general.SetupVersion

object StatisticsServer {
  def makeServer[F[_]: Async](env: Environment[F]): Resource[F, Server] = {
    val readerAlg = Reader.impl[F](env)

    val httpApp = (
      reader.Routes.routes[F](readerAlg)
    ).orNotFound
      
    EmberServerBuilder.default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(env.server.port).get)
      .withHttpApp(httpApp)
      .build
  }

  def run: IO[Nothing] = {
    implicit val ioAsync = IO.asyncForIO
    val resources = for {
      setupVersion <- Resource.eval(SetupVersion.impl())
      config <- Resource.eval(Config.impl(setupVersion))
      env <- Environment.impl(config)
      _ <- makeServer(env)
      _ <- KafkaInput.run(env)
    } yield ()
    resources.useForever
  }
}
