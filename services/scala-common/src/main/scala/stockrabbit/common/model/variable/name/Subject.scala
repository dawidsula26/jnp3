package stockrabbit.common.model.variable.name

import io.circe._

case class Subject(subject: String) extends AnyVal
object Subject {
  implicit val encoder = Encoder.encodeString.contramap[Subject](_.subject)
  implicit val decoder = Decoder.decodeString.map(Subject(_))
}
