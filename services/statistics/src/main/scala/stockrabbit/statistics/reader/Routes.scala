package stockrabbit.statistics.reader

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import stockrabbit.statistics.model.Variable
import java.time.Instant

object Routes {

  def routes[F[_]: Concurrent](R: Reader[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case request @ POST -> Root / "test" =>
        for {
          requestContent <- request.as[GetVariable.Request]
          responseContent <- R.getVariable(requestContent)
          response <- Ok(responseContent)
        } yield (response)
      case GET -> Root / "test" =>
        val requestContent = GetVariable.Request(
          Variable.Name("v"), 
          Some(Variable.Timestamp(Instant.ofEpochSecond(0))),
          Some(Variable.Timestamp(Instant.ofEpochSecond(1)))
        )
        (for {
          responseContent <- R.getVariable(requestContent)
          response <- Ok(responseContent)
        } yield (response))
        .handleErrorWith(err => {
          print(err)
          Ok("failed")
        })
    }
  }
}
