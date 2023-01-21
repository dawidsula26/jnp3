package stockrabbit.statistics.environment.mongo

import stockrabbit.common.environment.general.Address

case class ConfigMongo(
  address: Address,
  database: String
)
