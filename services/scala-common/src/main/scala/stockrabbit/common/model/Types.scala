package stockrabbit.common.model

import io.circe.generic.JsonCodec
import io.circe.Decoder
import io.circe.Encoder
import java.time.Instant

@JsonCodec case class Variable(
  name: Variable.Name, 
  value: Variable.Value, 
  time: Variable.Timestamp
)

object Variable {
  object Schema {
    val name = "name"
    val value = "value"
    val time = "time"
  }

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

  case class Timestamp(time: Instant) extends AnyVal
  object Timestamp {
    implicit val decoder = Decoder[Instant].map(Timestamp(_))
    implicit val encoder = Encoder[Instant].contramap[Timestamp](_.time)
  }
}
