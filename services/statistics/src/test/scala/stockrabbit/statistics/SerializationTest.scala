package stockrabbit.statistics

import munit.CatsEffectSuite
import stockrabbit.statistics.reader.GetVariable
import io.circe.syntax._
import io.circe._
import java.time.Instant
import stockrabbit.common.model.variable._
import stockrabbit.common.model.variable.name._

class SerializationTest extends CatsEffectSuite {
  test("should deserialize getVariable request without times") {
    val reqJson = Json.fromJsonObject(JsonObject(
      "variableName" -> Json.fromJsonObject(JsonObject(
        "statistic" -> Json.fromString("statT"),
        "strategy" -> Json.fromString("stratT"),
        "subject" -> Json.fromString("subjT")
      )),
      "startTime" -> Json.Null,
      "endTime" -> Json.Null
    ))
    val req = reqJson.as[GetVariable.Request].toOption.get
    val name = Name(Statistic("statT"), Some(Strategy("stratT")), Some(Subject("subjT")))
    val expected = GetVariable.Request(name, None, None)
    assertEquals(req, expected)
  }

  test("should deserialize getVariable request with times") {
    val reqJson = Json.fromJsonObject(JsonObject(
      "variableName" -> Json.fromJsonObject(JsonObject(
        "statistic" -> Json.fromString("statT"),
        "strategy" -> Json.fromString("stratT"),
        "subject" -> Json.fromString("subjT")
      )),
      "startTime" -> Json.fromString("1970-01-01T00:00:00Z"),
      "endTime" -> Json.fromString("1970-01-01T00:00:12Z")
    ))
    val req = reqJson.as[GetVariable.Request].toOption.get
    val expected = GetVariable.Request(
      Name(Statistic("statT"), Some(Strategy("stratT")), Some(Subject("subjT"))), 
      Some(Timestamp(Instant.ofEpochSecond(0))), 
      Some(Timestamp(Instant.ofEpochSecond(12)))
    )
    assertEquals(req, expected)
  }

  test("should serialize getVariable response") {
    val name = Name(Statistic("statT"), Some(Strategy("stratT")), Some(Subject("subjT")))
    val resp = GetVariable.Response(
      name,
      Seq(
        (Value(4.87), Timestamp(Instant.ofEpochSecond(6))),
        (Value(3.125), Timestamp(Instant.ofEpochSecond(15))),
        (Value(9.5), Timestamp(Instant.ofEpochSecond(25)))
      )
    )
    val respJson = resp.asJson

    val nameJson = Json.fromJsonObject(JsonObject(
      "statistic" -> Json.fromString("statT"),
      "strategy" -> Json.fromString("stratT"),
      "subject" -> Json.fromString("subjT")
    ))
    
    def variableJson(v: Double, t: String): Json = 
      Json.arr(Json.fromDouble(v).get, Json.fromString(t))

    val expectedJson = Json.fromJsonObject(JsonObject(
      "variableName" -> nameJson,
      "values" -> Json.fromValues(Seq(
        variableJson(4.87, "1970-01-01T00:00:06Z"),
        variableJson(3.125, "1970-01-01T00:00:15Z"),
        variableJson(9.5, "1970-01-01T00:00:25Z")
      ))
    ))
    assertEquals(respJson, expectedJson)
  }
}
