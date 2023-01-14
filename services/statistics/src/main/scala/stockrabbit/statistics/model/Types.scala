package stockrabbit.statistics.model

import io.circe.generic.JsonCodec
import io.circe.Decoder
import io.circe.Encoder

case class VariableName(name: String) extends AnyVal
object VariableName {
  implicit val decoder = Decoder.decodeString.map(VariableName(_))
  implicit val encoder = Encoder.encodeString.contramap[VariableName](_.name)
}

case class Value(value: Double) extends AnyVal
object Value {
  implicit val decoder = Decoder.decodeDouble.map(Value(_))
  implicit val encoder = Encoder.encodeDouble.contramap[Value](_.value)
}

case class Timestamp(time: String) extends AnyVal
object Timestamp {
  implicit val decoder = Decoder.decodeString.map(Timestamp(_))
  implicit val encoder = Encoder.encodeString.contramap[Timestamp](_.time)
}

@JsonCodec case class ValueWithTimestamp(value: Value, timestamp: Timestamp)

@JsonCodec case class Variable(
  name: VariableName, 
  startTime: Timestamp,
  endTime: Timestamp,
  values: List[ValueWithTimestamp]
)
