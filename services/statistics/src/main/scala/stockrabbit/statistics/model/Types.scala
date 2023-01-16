package stockrabbit.statistics.model

import io.circe.generic.JsonCodec
import io.circe.Decoder
import io.circe.Encoder
import java.time.Instant
import mongo4cats.bson.Document
import mongo4cats.bson.BsonValue

@JsonCodec case class Variable(
  name: Variable.Name, 
  value: Variable.Value, 
  time: Variable.Timestamp
) {
  def document: Document = Document(
    Variable.Schema.name -> name.document,
    Variable.Schema.value -> value.document,
    Variable.Schema.time -> time.document
  )
}

object Variable {
  object Schema {
    val name = "name"
    val value = "value"
    val time = "time"
  }

  case class Name(name: String) extends AnyVal {
    def document: BsonValue = BsonValue.string(name)
  }
  object Name {
    implicit val decoder = Decoder[String].map(Name(_))
    implicit val encoder = Encoder[String].contramap[Name](_.name)
  }

  case class Value(value: Double) extends AnyVal {
    def document: BsonValue = BsonValue.double(value)
  }
  object Value {
    implicit val decoder = Decoder[Double].map(Value(_))
    implicit val encoder = Encoder[Double].contramap[Value](_.value)
  }

  case class Timestamp(time: Instant) extends AnyVal {
    def document: BsonValue = BsonValue.instant(time)
  }
  object Timestamp {
    implicit val decoder = Decoder[Instant].map(Timestamp(_))
    implicit val encoder = Encoder[Instant].contramap[Timestamp](_.time)
  }
}
