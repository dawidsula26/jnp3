package stockrabbit.calculations

import stockrabbit.calculations.environment.general.Environment
import stockrabbit.calculations.environment.general.Config
import stockrabbit.calculations.manager.Manager

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.Server
import stockrabbit.calculations.kafka.KafkaInput
import stockrabbit.common.environment.general.SetupVersion

object StatisticsServer {
  def makeServer[F[_]: Async](env: Environment[F]): Resource[F, Server] = {
    val managerAlg = Manager.impl[F](env)

    val httpApp = (
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
