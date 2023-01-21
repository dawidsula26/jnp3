package stockrabbit.calculations

import munit.CatsEffectSuite
import stockrabbit.calculations.reader.GetVariable
import stockrabbit.calculations.model.Variable
import io.circe.syntax._
import io.circe._
import java.time.Instant

class SerializationTest extends CatsEffectSuite {
  test("should deserialize getVariable request without times") {
    val reqJson = Json.fromJsonObject(JsonObject(
      "variableName" -> Json.fromString("t"),
      "startTime" -> Json.Null,
      "endTime" -> Json.Null
    ))
    val req = reqJson.as[GetVariable.Request].toOption.get
    val expected = GetVariable.Request(Variable.Name("t"), None, None)
    assertEquals(req, expected)
  }

  test("should deserialize getVariable request with times") {
    val reqJson = Json.fromJsonObject(JsonObject(
      "variableName" -> Json.fromString("t"),
      "startTime" -> Json.fromString("1970-01-01T00:00:00Z"),
      "endTime" -> Json.fromString("1970-01-01T00:00:12Z")
    ))
    val req = reqJson.as[GetVariable.Request].toOption.get
    val expected = GetVariable.Request(
      Variable.Name("t"), 
      Some(Variable.Timestamp(Instant.ofEpochSecond(0))), 
      Some(Variable.Timestamp(Instant.ofEpochSecond(12)))
    )
    assertEquals(req, expected)
  }

  test("should serialize getVariable response") {
    val resp = GetVariable.Response(
      Variable.Name("t"),
      Seq(
        Variable(Variable.Name("t"), Variable.Value(4.87), Variable.Timestamp(Instant.ofEpochSecond(6))),
        Variable(Variable.Name("t"), Variable.Value(3.125), Variable.Timestamp(Instant.ofEpochSecond(15))),
        Variable(Variable.Name("t"), Variable.Value(9.5), Variable.Timestamp(Instant.ofEpochSecond(25)))
      )
    )
    val respJson = resp.asJson

    def variableJson(n: String, v: Double, t: String): Json = 
      Json.fromJsonObject(JsonObject(
        Variable.Schema.name -> Json.fromString(n),
        Variable.Schema.value -> Json.fromDouble(v).get,
        Variable.Schema.time -> Json.fromString(t)
      ))
    val expectedJson = Json.fromJsonObject(JsonObject(
      "variableName" -> Json.fromString("t"),
      "values" -> Json.fromValues(Seq(
        variableJson("t", 4.87, "1970-01-01T00:00:06Z"),
        variableJson("t", 3.125, "1970-01-01T00:00:15Z"),
        variableJson("t", 9.5, "1970-01-01T00:00:25Z")
      ))
    ))
    assertEquals(respJson, expectedJson)
  }
}
