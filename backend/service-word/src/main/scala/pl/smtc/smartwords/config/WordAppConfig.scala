package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory

final case class WordServiceConfig(
  name: String,
  host: String,
  port: Int,
  idleTimeoutMinutes: Int
)

final case class WordCorsConfig(
  anyOrigin: Boolean,
  allowCredentials: Boolean,
  maxAgeSeconds: Long,
  anyMethod: Boolean
)

final case class WordDataConfig(dictionaryExtension: String)

final case class WordAppConfig(
  service: WordServiceConfig,
  cors: WordCorsConfig,
  data: WordDataConfig
)

object WordAppConfig {

  def load(): WordAppConfig = {
    val config = ConfigFactory.load()

    WordAppConfig(
      service = WordServiceConfig(
        name = config.getString("service.name"),
        host = config.getString("service.host"),
        port = config.getInt("service.port"),
        idleTimeoutMinutes = config.getInt("service.idle-timeout-minutes")
      ),
      cors = WordCorsConfig(
        anyOrigin = config.getBoolean("cors.any-origin"),
        allowCredentials = config.getBoolean("cors.allow-credentials"),
        maxAgeSeconds = config.getLong("cors.max-age-seconds"),
        anyMethod = config.getBoolean("cors.any-method")
      ),
      data = WordDataConfig(
        dictionaryExtension = config.getString("data.dictionary-extension")
      )
    )
  }
}
