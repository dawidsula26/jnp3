package stockrabbit.statistics.reader

import stockrabbit.statistics.model._

import cats.effect._
import cats.implicits._
import stockrabbit.statistics.model.Variable
import mongo4cats.client.MongoClient
import mongo4cats.bson.Document
import mongo4cats.operations.Filter
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Reader[F[_]]{
  def mongoTest(): F[Unit]

  def getVariable(n: GetVariable.Request): F[GetVariable.Response]
}

object Reader {
  def impl[F[_]: Sync](mongoClient: MongoClient[F]): Reader[F] = new Reader[F]{
    def mongoTest(): F[Unit] = {
      for {
        database <- mongoClient.getDatabase("testDB")
        _ <- database.createCollection("testCollection")
        collection <- database.getCollection("testCollection")

        document = Document().add("a" -> 48).add("b" -> 64)
        _ <- collection.insertOne(document)
        documentOption <- collection.find(Filter.eq("a", 48)).first

        logger <- Slf4jLogger.create[F]
        _ <- logger.info(documentOption.toString())
      } yield ()
    }

    def getVariable(request: GetVariable.Request): F[GetVariable.Response] = {
      val variable = Variable(
        name = VariableName(request.variableName),
        startTime = Timestamp("start"),
        endTime = Timestamp("end"),
        values = List(
          ValueWithTimestamp(Value(4), Timestamp("first")), 
          ValueWithTimestamp(Value(9), Timestamp("second"))
        )
      )
      GetVariable.Response(variable).pure[F]
    }
  }
}
