package stockrabbit.common.environment.general

case class Address(host: String, port: Int) {
  def str: String = host + ":" + port
}
