package stockrabbit.statistics.mongo

import mongo4cats.collection.MongoCollection
import mongo4cats.bson.Document
import mongo4cats.database.MongoDatabase
import cats._
import cats.implicits._
import com.mongodb.client.model.TimeSeriesOptions
import mongo4cats.models.database.CreateCollectionOptions
import mongo4cats.operations
import stockrabbit.common.model.variable._
import stockrabbit.statistics.mongo.Serialization._

class VariableCollection[F[_]](collection: MongoCollection[F, Document]) {
  def insert(xs: Seq[Variable]) =
    collection.insertMany(xs.map(_.document))

  def find(filters: Seq[VariableCollection.Filter]) = {
    val filter = filters
      .map(_.filter)
      .reduce((f: operations.Filter, g: operations.Filter) => f && g)
    collection.find(filter)
  }
}

object VariableCollection {
  val name = "variables"

  def initialize[F[_]](database: MongoDatabase[F]): F[Unit] = {
    val optionsTimeseries = 
      new TimeSeriesOptions(Variable.Schema.time)
      .metaField(Variable.Schema.name)
    val optionsCollection = CreateCollectionOptions().timeSeriesOptions(optionsTimeseries)
    database.createCollection(name, optionsCollection)
  }

  def get[F[_]: Functor](database: MongoDatabase[F]): F[VariableCollection[F]] = for {
    collection <- database.getCollection(name)
  } yield (new VariableCollection(collection))

  
  case class Filter private(filter: operations.Filter)
  object Filter {
    def eq(name: Name): Filter = 
      Filter(operations.Filter.eq(Variable.Schema.name, name.bson))
    def eq(time: Timestamp): Filter = 
      Filter(operations.Filter.eq(Variable.Schema.time, time.bson))

    def gte(time: Timestamp): Filter =
      Filter(operations.Filter.gte(Variable.Schema.time, time.bson))
    def gt(time: Timestamp): Filter = 
      Filter(operations.Filter.gt(Variable.Schema.time, time.bson))
    
    def lte(time: Timestamp): Filter =
      Filter(operations.Filter.lte(Variable.Schema.time, time.bson))
    def lt(time: Timestamp): Filter = 
      Filter(operations.Filter.lt(Variable.Schema.time, time.bson))

    def empty: Filter = Filter(operations.Filter.empty)
  }
}