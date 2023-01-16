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
    implicit val decoder = Decoder[String].map(Name(_))
    implicit val encoder = Encoder[String].contramap[Name](_.name)
  }

  case class Value(value: Double) extends AnyVal
  object Value {
    implicit val decoder = Decoder[Double].map(Value(_))
    implicit val encoder = Encoder[Double].contramap[Value](_.value)
  }

  case class Timestamp(time: Int) extends AnyVal
  object Timestamp {
    implicit val decoder = Decoder[Int].map(Timestamp(_))
    implicit val encoder = Encoder[Int].contramap[Timestamp](_.time)
  }
}
