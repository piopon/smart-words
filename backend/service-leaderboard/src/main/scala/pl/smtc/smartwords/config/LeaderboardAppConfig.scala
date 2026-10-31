package pl.smtc.smartwords.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

final case class LeaderboardServiceConfig(
  name: String,
  host: String,
  port: Int,
  idleTimeoutMinutes: Int
)

final case class LeaderboardCorsConfig(
  anyOrigin: Boolean,
  allowCredentials: Boolean,
  maxAgeSeconds: Long,
  anyMethod: Boolean
)

final case class LeaderboardDataConfig(
  file: String,
  dataDir: Option[String]
)

final case class LeaderboardAppConfig(
  service: LeaderboardServiceConfig,
  cors: LeaderboardCorsConfig,
  data: LeaderboardDataConfig
)

object LeaderboardAppConfig {

  private def optionalString(config: Config, configPath: String): Option[String] = {
    if (!config.hasPath(configPath)) {
      None
    } else {
      val value = config.getString(configPath).trim
      if (value.isEmpty) None else Some(value)
    }
  }

  def load(config: Config): LeaderboardAppConfig = {
    LeaderboardAppConfig(
      service = LeaderboardServiceConfig(
        name = config.getString("service.name"),
        host = config.getString("service.host"),
        port = config.getInt("service.port"),
        idleTimeoutMinutes = config.getInt("service.idle-timeout-minutes")
      ),
      cors = LeaderboardCorsConfig(
        anyOrigin = config.getBoolean("cors.any-origin"),
        allowCredentials = config.getBoolean("cors.allow-credentials"),
        maxAgeSeconds = config.getLong("cors.max-age-seconds"),
        anyMethod = config.getBoolean("cors.any-method")
      ),
      data = LeaderboardDataConfig(
        file = config.getString("data.file"),
        dataDir = optionalString(config, "data.dir")
      )
    )
  }

  def load(): LeaderboardAppConfig = load(ConfigFactory.load())
}
