package stockrabbit.statistics.model

import io.circe.generic.JsonCodec
import io.circe.Decoder
import io.circe.Encoder
import java.time.Instant
import mongo4cats.bson.Document
import mongo4cats.bson.BsonValue
import org.bson.BsonSerializationException

@JsonCodec case class Variable(
  name: Variable.Name, 
  value: Variable.Value, 
  time: Variable.Timestamp
) {
  def document: Document = Document(
    Variable.Schema.name -> name.bson,
    Variable.Schema.value -> value.bson,
    Variable.Schema.time -> time.bson
  )
}

object Variable {
  def fromDocument(d: Document): Variable = (for {
    nameBson <- d.get(Variable.Schema.name)
    valueBson <- d.get(Variable.Schema.value)
    timeBson <- d.get(Variable.Schema.time)
  } yield (Variable(
    Name.fromBson(nameBson),
    Value.fromBson(valueBson),
    Timestamp.fromBson(timeBson)
  ))).getOrElse(throw new BsonSerializationException(d.toString()))
    
  object Schema {
    val name = "name"
    val value = "value"
    val time = "time"
  }

  case class Name(name: String) extends AnyVal {
    def bson: BsonValue = BsonValue.string(name)
  }
  object Name {
    def fromBson(v: BsonValue): Name = (for {
      name <- v.asString
    } yield (Name(name))).getOrElse(throw new BsonSerializationException(v.toString()))

    implicit val decoder = Decoder[String].map(Name(_))
    implicit val encoder = Encoder[String].contramap[Name](_.name)
  }

  case class Value(value: Double) extends AnyVal {
    def bson: BsonValue = BsonValue.double(value)
  }
  object Value {
    def fromBson(v: BsonValue): Value = (for {
      value <- v.asDouble
    } yield (Value(value))).getOrElse(throw new BsonSerializationException(v.toString()))

    implicit val decoder = Decoder[Double].map(Value(_))
    implicit val encoder = Encoder[Double].contramap[Value](_.value)
  }

  case class Timestamp(time: Instant) extends AnyVal {
    def bson: BsonValue = BsonValue.instant(time)
  }
  object Timestamp {
    def fromBson(v: BsonValue): Timestamp = (for {
      time <- v.asInstant
    } yield (Timestamp(time))).getOrElse(throw new BsonSerializationException(v.toString()))

    implicit val decoder = Decoder[Instant].map(Timestamp(_))
    implicit val encoder = Encoder[Instant].contramap[Timestamp](_.time)
  }
}
