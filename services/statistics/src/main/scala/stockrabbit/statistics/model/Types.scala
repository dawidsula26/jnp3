package stockrabbit.statistics.model

import io.circe.generic.JsonCodec
import io.circe.Decoder
import io.circe.Encoder

@JsonCodec case class Variable(
  name: Variable.Name, 
  value: Variable.Value, 
  timestamp: Variable.Timestamp
)

object Variable {
  case class Name(name: String) extends AnyVal
  object Name {
    implicit val decoder = Decoder.decodeString.map(Name(_))
    implicit val encoder = Encoder.encodeString.contramap[Name](_.name)
  }

  case class Value(value: Double) extends AnyVal
  object Value {
    implicit val decoder = Decoder.decodeDouble.map(Value(_))
    implicit val encoder = Encoder.encodeDouble.contramap[Value](_.value)
  }

  case class Timestamp(time: Int) extends AnyVal
  object Timestamp {
    implicit val decoder = Decoder.decodeInt.map(Timestamp(_))
    implicit val encoder = Encoder.encodeInt.contramap[Timestamp](_.time)
  }
}

@JsonCodec case class VariableTimeseries(
  name: Variable.Name, 
  startTime: Variable.Timestamp,
  endTime: Variable.Timestamp,
  values: Seq[Variable]
)
