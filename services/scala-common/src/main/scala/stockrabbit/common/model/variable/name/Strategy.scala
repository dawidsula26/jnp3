package stockrabbit.common.model.variable.name

import io.circe._

case class Strategy(strategy: String) extends AnyVal
object Strategy {
  implicit val encoder = Encoder.encodeString.contramap[Strategy](_.strategy)
  implicit val decoder = Decoder.decodeString.map(Strategy(_))
}
