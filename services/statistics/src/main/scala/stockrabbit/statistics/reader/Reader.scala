package stockrabbit.statistics.reader

import stockrabbit.statistics.environment.general.Environment

import cats.effect._
import mongo4cats.operations.Filter
import cats.implicits._
import stockrabbit.statistics.model.Variable
import mongo4cats.circe._
import java.time.Instant

trait Reader[F[_]]{
  def getVariable(n: GetVariable.Request): F[GetVariable.Response]
}

object Reader {
  def impl[F[_]: Sync](env: Environment[F]): Reader[F] = new Reader[F]{
    def getVariable(request: GetVariable.Request): F[GetVariable.Response] = {
      val database = env.mongo.database
      val filterName = Filter.eq(Variable.Schema.name, request.variableName)
      val filterStartTime = request.startTime match {
        case Some(t) => Filter.gte(Variable.Schema.time, t)
        case None => Filter.empty
      }
      val filterEndTime = request.endTime match {
        case Some(t) => Filter.lt(Variable.Schema.time, t)
        case None => Filter.empty
      }
      for {
        collection <- database.getCollectionWithCodec[Variable](env.mongo.collectionName)
        variables <- collection
          .withAddedCodec[Variable.Name]
          .withAddedCodec[Variable.Timestamp]
          .find(filterName && filterStartTime && filterEndTime)
          .all
      } yield (GetVariable.Response(
        request.variableName,
        Variable.Timestamp(Instant.ofEpochSecond(0)),
        Variable.Timestamp(Instant.ofEpochSecond(1)),
        variables.toSeq
      ))
    }
  }
}
