package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

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

final case class WordDataConfig(
  dictionaryExtension: String,
  dataDir: Option[String],
  seedDir: Option[String]
)

final case class WordAppConfig(
  service: WordServiceConfig,
  cors: WordCorsConfig,
  data: WordDataConfig
)

object WordAppConfig {

  private def optionalString(config: Config, configPath: String): Option[String] = {
    if (!config.hasPath(configPath)) {
      None
    } else {
      val value = config.getString(configPath).trim
      if (value.isEmpty) None else Some(value)
    }
  }

  def load(config: Config): WordAppConfig = {
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
        dictionaryExtension = config.getString("data.dictionary-extension"),
        dataDir = optionalString(config, "data.dir"),
        seedDir = optionalString(config, "data.seed-dir")
      )
    )
  }

  def load(): WordAppConfig = load(ConfigFactory.load())
}
