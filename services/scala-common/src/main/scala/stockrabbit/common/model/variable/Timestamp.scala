package stockrabbit.common.model.variable

import java.time.Instant
import io.circe._

case class Timestamp(time: Instant) extends AnyVal
object Timestamp {
  implicit val decoder = Decoder[Instant].map(Timestamp(_))
  implicit val encoder = Encoder[Instant].contramap[Timestamp](_.time)
}
