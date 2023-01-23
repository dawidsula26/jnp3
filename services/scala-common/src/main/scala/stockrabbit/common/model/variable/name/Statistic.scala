package stockrabbit.common.model.variable.name

import io.circe._

case class Statistic(statistic: String) extends AnyVal
object Statistic {
  implicit val encoder = Encoder.encodeString.contramap[Statistic](_.statistic)
  implicit val decoder = Decoder.decodeString.map(Statistic(_))
}
