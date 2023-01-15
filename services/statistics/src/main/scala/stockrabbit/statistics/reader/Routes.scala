package stockrabbit.statistics.reader

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Routes {

  def routes[F[_]: Concurrent](R: Reader[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "testMongo" =>
        for {
          _ <- R.mongoTest()
          response <- Ok("ok")
        } yield (response)

      case request @ POST -> Root / "test" =>
        for {
          requestContent <- request.as[GetVariable.Request]
          responseContent <- R.getVariable(requestContent)
          response <- Ok(responseContent)
        } yield (response)
      case GET -> Root / "test" =>
        val requestContent = GetVariable.Request("t")
        for {
          responseContent <- R.getVariable(requestContent)
          response <- Ok(responseContent)
        } yield (response)
    }
  }
}
