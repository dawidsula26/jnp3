package stockrabbit.statistics.environment.mongo

case class ConfigMongo(
  address: Address,
  database: String
)

case class Address(host: String, port: Int)
