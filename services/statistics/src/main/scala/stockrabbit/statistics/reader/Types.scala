package stockrabbit.statistics.reader

import io.circe.generic.JsonCodec
import cats.effect.Concurrent
import org.http4s._
import org.http4s.circe._
import stockrabbit.common.model.variable._

object GetVariable {
  @JsonCodec(decodeOnly = true) case class Request(
    variableName: Name, 
    startTime: Option[Timestamp],
    endTime: Option[Timestamp]
  )
  object Request {
    implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, Request] =
      jsonOf
  }

  @JsonCodec(encodeOnly = true) case class Response(
    variableName: Name,
    values: Seq[Variable]
  )
  object Response {
    implicit def encoder[F[_]]: EntityEncoder[F, Response] =
      jsonEncoderOf
  }
}
