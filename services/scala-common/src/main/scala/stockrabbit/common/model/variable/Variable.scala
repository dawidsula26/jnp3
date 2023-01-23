package stockrabbit.common.model.variable

import io.circe.generic.JsonCodec
import stockrabbit.common.model.variable.Timestamp
import stockrabbit.common.model.variable.Value

@JsonCodec case class Variable(
  name: Name, 
  value: Value, 
  time: Timestamp
)

object Variable {
  object Schema {
    val name = "name"
    val value = "value"
    val time = "time"
  }
}
