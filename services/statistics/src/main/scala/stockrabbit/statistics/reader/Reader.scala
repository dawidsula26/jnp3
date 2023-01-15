package stockrabbit.statistics.reader

import stockrabbit.statistics.model.VariableTimeseries
import stockrabbit.statistics.environment.general.Environment

import cats.effect._
import mongo4cats.operations.Filter
import cats.implicits._
import stockrabbit.statistics.model.Variable
import mongo4cats.circe._

trait Reader[F[_]]{
  def getVariable(n: GetVariable.Request): F[GetVariable.Response]
}

object Reader {
  def impl[F[_]: Sync](env: Environment[F]): Reader[F] = new Reader[F]{
    def getVariable(request: GetVariable.Request): F[GetVariable.Response] = {
      val database = env.mongo.database
      for {
        collection <- database.getCollectionWithCodec[Variable]("variables")
        variables <- collection.find(Filter.empty).all
      } yield (GetVariable.Response(VariableTimeseries(
        request.variableName,
        Variable.Timestamp(0),
        Variable.Timestamp(1),
        variables.toSeq
      )))
    }
  }
}
