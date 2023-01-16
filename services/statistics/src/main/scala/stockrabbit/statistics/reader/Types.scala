package stockrabbit.statistics.reader

import stockrabbit.statistics.model._

import io.circe.generic.JsonCodec


import cats.effect.Concurrent
import org.http4s._
import org.http4s.circe._

object GetVariable {
  @JsonCodec(decodeOnly = true) case class Request(
    variableName: Variable.Name, 
    startTime: Option[Variable.Timestamp],
    endTime: Option[Variable.Timestamp]
  )
  object Request {
    implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, Request] =
      jsonOf
  }

  @JsonCodec(encodeOnly = true) case class Response(
    variableName: Variable.Name,
    startTime: Variable.Timestamp,
    endTime: Variable.Timestamp,
    values: Seq[Variable]
  )
  object Response {
    implicit def encoder[F[_]]: EntityEncoder[F, Response] =
      jsonEncoderOf
  }
}
