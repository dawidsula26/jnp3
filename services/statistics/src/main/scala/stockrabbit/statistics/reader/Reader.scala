package stockrabbit.statistics.reader

import stockrabbit.statistics.environment.general.Environment

import cats.effect._
import cats.implicits._
import stockrabbit.statistics.model.Variable
import stockrabbit.statistics.mongo.VariableCollection

trait Reader[F[_]]{
  def getVariable(n: GetVariable.Request): F[GetVariable.Response]
}

object Reader {
  def impl[F[_]: Async](env: Environment[F]): Reader[F] = new Reader[F]{
    def getVariable(request: GetVariable.Request): F[GetVariable.Response] = {
      val database = env.mongo.database
      val filterName = VariableCollection.Filter.eq(request.variableName)
      val filterStartTime = request.startTime match {
        case Some(t) => VariableCollection.Filter.gte(t)
        case None => VariableCollection.Filter.empty
      }
      val filterEndTime = request.endTime match {
        case Some(t) => VariableCollection.Filter.lt(t)
        case None => VariableCollection.Filter.empty
      }
      for {
        collection <- VariableCollection.get(database)
        variables <- collection.find(Seq(filterName, filterStartTime, filterEndTime)).all
      } yield (GetVariable.Response(
        request.variableName,
        variables.toSeq.map(Variable.fromDocument(_))
      ))
    }
  }
}
