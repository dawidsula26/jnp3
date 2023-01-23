package stockrabbit.statistics.mongo

import mongo4cats.bson.Document
import mongo4cats.bson.BsonValue
import org.bson.BsonSerializationException
import stockrabbit.common.model.variable._
import stockrabbit.common.model.variable.name._

trait DocumentSerialization {
  def document: Document
  def bson: BsonValue = BsonValue.document(document)
}

trait BsonDeserialization[T] {
  def fromBsonOption(v: BsonValue): Option[T]
  def fromBson(v: BsonValue): T = 
    fromBsonOption(v).getOrElse(throw new BsonSerializationException(v.toString()))
}
object BsonDeserialization {
  def deserializeOption[T](v: BsonValue, f: BsonValue => Option[T]): Option[Option[T]] = 
    if (v.isNull) Some(None) else f(v).map(Some(_))
}

trait DocumentDeserialization[T] extends BsonDeserialization[T] {
  def fromDocumentOption(d: Document): Option[T]
  def fromDocument(d: Document) = 
    fromDocumentOption(d).getOrElse(throw new BsonSerializationException(d.toString()))

  def fromBsonOption(v: BsonValue): Option[T] = 
    v.asDocument.flatMap(fromDocumentOption(_))
}

object Serialization {
  implicit class VariableDocument(v: Variable) extends DocumentSerialization {
    def document: Document = Document(
      Variable.Schema.name -> v.name.bson,
      Variable.Schema.value -> v.value.bson,
      Variable.Schema.time -> v.time.bson
    )
  }

  object VariableDocument extends DocumentDeserialization[Variable] {
    def fromDocumentOption(d: Document): Option[Variable] = for {
      name <- d.get(Variable.Schema.name).flatMap(NameBson.fromBsonOption(_))
      value <- d.get(Variable.Schema.value).flatMap(ValueBson.fromBsonOption(_))
      time <- d.get(Variable.Schema.time).flatMap(TimestampBson.fromBsonOption(_))
    } yield (Variable(name, value, time))
  }


  implicit class NameDocument(n: Name) extends DocumentSerialization{
    def document: Document = Document(
      Name.Schema.statistic -> n.statistic.bson,
      Name.Schema.strategy -> n.strategy.map(_.bson).getOrElse(BsonValue.Null),
      Name.Schema.subject -> n.subject.map(_.bson).getOrElse(BsonValue.Null)
    )
  }
  object NameBson extends DocumentDeserialization[Name] {
    def fromDocumentOption(d: Document): Option[Name] = for {
      statistic <- d.get(Name.Schema.statistic).flatMap(StatisticBson.fromBsonOption(_))
      strategy <- d
        .get(Name.Schema.strategy)
        .flatMap(BsonDeserialization.deserializeOption[Strategy](_, StrategyBson.fromBsonOption(_)))
      subject <- d
        .get(Name.Schema.subject)
        .flatMap(BsonDeserialization.deserializeOption[Subject](_, SubjectBson.fromBsonOption(_)))
    } yield (Name(statistic, strategy, subject))
  }


  implicit class StatisticBson(s: Statistic) {
    def bson: BsonValue = BsonValue.string(s.statistic)
  }
  object StatisticBson extends BsonDeserialization[Statistic] {
    def fromBsonOption(v: BsonValue): Option[Statistic] = v.asString.map(Statistic(_))
  }

  implicit class StrategyBson(s: Strategy) {
    def bson: BsonValue = BsonValue.string(s.strategy)
  }
  object StrategyBson extends BsonDeserialization[Strategy] {
    def fromBsonOption(v: BsonValue): Option[Strategy] = v.asString.map(Strategy(_))
  }

  implicit class SubjectBson(s: Subject) {
    def bson: BsonValue = BsonValue.string(s.subject)
  }
  object SubjectBson extends BsonDeserialization[Subject] {
    def fromBsonOption(v: BsonValue): Option[Subject] = v.asString.map(Subject(_))
  }


  implicit class ValueBson(v: Value) {
    def bson: BsonValue = BsonValue.double(v.value)
  }
  object ValueBson extends BsonDeserialization[Value] {
    def fromBsonOption(v: BsonValue): Option[Value] = for {
      value <- v.asDouble
    } yield (Value(value))
  }

  implicit class TimestampBson(t: Timestamp) {
    def bson: BsonValue = BsonValue.instant(t.time)
  }
  object TimestampBson extends BsonDeserialization[Timestamp] {
    def fromBsonOption(v: BsonValue): Option[Timestamp] = for {
      time <- v.asInstant
    } yield (Timestamp(time))
  }
}