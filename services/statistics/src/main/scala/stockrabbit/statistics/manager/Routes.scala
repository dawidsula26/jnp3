package stockrabbit.statistics.manager

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Routes {

  def routes[F[_]: Concurrent](M: Manager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    val basePath = Root / "manager"
    HttpRoutes.of[F] {
      case GET -> `basePath` / "initializeDatabase" =>
        for {
          _ <- M.initializeDatabase()
          response <- Ok("ok")
        } yield (response)
    }
  }
}
