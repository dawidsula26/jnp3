package stockrabbit.common.model.variable

import io.circe._

case class Value(value: Double) extends AnyVal
object Value {
  implicit val decoder = Decoder[Double].map(Value(_))
  implicit val encoder = Encoder[Double].contramap[Value](_.value)
}
