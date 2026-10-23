package pl.smtc.smartwords.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

final case class LeaderboardServiceConfig(
  name: String,
  host: String,
  port: Int,
  idleTimeoutMinutes: Int
)

final case class LeaderboardAppConfig(
  service: LeaderboardServiceConfig,
)

object LeaderboardAppConfig {

  def load(config: Config): LeaderboardAppConfig = {
    LeaderboardAppConfig(
      service = LeaderboardServiceConfig(
        name = config.getString("service.name"),
        host = config.getString("service.host"),
        port = config.getInt("service.port"),
        idleTimeoutMinutes = config.getInt("service.idle-timeout-minutes")
      )
    )
  }

  def load(): LeaderboardAppConfig = load(ConfigFactory.load())
}
