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
          response <- Ok("initialized")
        } yield (response)

      case GET -> `basePath` / "removeDatabase" =>
        for {
          _ <- M.removeDatabase()
          response <- Ok("removed")
        } yield (response)

      case GET -> `basePath` / "fillWithGarbage" =>
        (for {
          _ <- M.fillWithGarbage()
          response <- Ok("filled with garbage")
        } yield (response))
        .handleErrorWith(err =>{
          print(err)
          Ok("error")
        })
    }
  }
}
