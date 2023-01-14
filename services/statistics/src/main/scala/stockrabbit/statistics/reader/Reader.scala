package stockrabbit.statistics.reader

import stockrabbit.statistics.model._

import cats.Applicative
import cats.implicits._
import stockrabbit.statistics.model.Variable

trait Reader[F[_]]{
  def getVariable(n: GetVariable.Request): F[GetVariable.Response]
}

object Reader {
  def impl[F[_]: Applicative]: Reader[F] = new Reader[F]{
    def getVariable(request: GetVariable.Request): F[GetVariable.Response] = {
      val variable = Variable(
        name = VariableName(request.variableName),
        startTime = Timestamp("start"),
        endTime = Timestamp("end"),
        values = List(
          ValueWithTimestamp(Value(4), Timestamp("first")), 
          ValueWithTimestamp(Value(9), Timestamp("second"))
        )
      )
      GetVariable.Response(variable).pure[F]
    }
  }
}
