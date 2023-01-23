package stockrabbit.common.model.variable

import io.circe.generic._
import stockrabbit.common.model.variable.name.Statistic
import stockrabbit.common.model.variable.name.Subject
import stockrabbit.common.model.variable.name.Strategy

@JsonCodec case class Name(
  statistic: Statistic, 
  strategy: Option[Strategy], 
  subject: Option[Subject]
)

object Name {
  object Schema {
    val strategy = "strategy"
    val subject = "subject"
    val statistic = "statistic"
  }
}
