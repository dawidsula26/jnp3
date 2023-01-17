package stockrabbit.statistics.reader

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Routes {

  def routes[F[_]: Concurrent](R: Reader[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    val basePath = Root / "reader"
    HttpRoutes.of[F] {
      case request @ POST -> `basePath` / "getVariable" =>
        for {
          requestContent <- request.as[GetVariable.Request]
          responseContent <- R.getVariable(requestContent)
          response <- Ok(responseContent)
        } yield (response)
    }
  }
}
