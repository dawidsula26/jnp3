package stockrabbit.statistics.environment.mongo

import stockrabbit.statistics.environment.general.Address

case class ConfigMongo(
  address: Address,
  database: String
)
