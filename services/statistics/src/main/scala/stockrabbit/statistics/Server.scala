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
import org.http4s.server.Server
import stockrabbit.statistics.kafka.KafkaInput
import stockrabbit.common.environment.general.SetupVersion

object StatisticsServer {
  def makeServer[F[_]: Async](env: Environment[F]): Resource[F, Server] = {
    val readerAlg = Reader.impl[F](env)
    val managerAlg = Manager.impl[F](env)

    val httpApp = (
      reader.Routes.routes[F](readerAlg) <+> 
        reader.Routes.routes[F](readerAlg) <+> 
      reader.Routes.routes[F](readerAlg) <+> 
      manager.Routes.routes[F](managerAlg)
    ).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
      
    EmberServerBuilder.default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(env.server.port).get)
      .withHttpApp(finalHttpApp)
      .build
  }

  def makeKafka(env: Environment[IO]): IO[Unit] = {
    for {
      _ <- KafkaInput.run(env).start
    } yield ()
  }

  def run: IO[Nothing] = {
    implicit val ioAsync = IO.asyncForIO
    val resources = for {
      setupVersion <- Resource.eval(SetupVersion.impl())
      config <- Resource.eval(Config.impl(setupVersion))
      env <- Environment.impl(config)
      _ <- makeServer(env)
      _ <- Resource.eval(makeKafka(env))
    } yield ()
    resources.useForever
  }
}
