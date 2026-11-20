package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite

class LeaderboardAppConfigTest extends AnyFunSuite {

  test("testLoadReadsConfiguredValuesAndNormalizesOptionalDataDirectory") {
    val testConfig = ConfigFactory.parseString(
      """
        service {
          name = "LEADERBOARD-TEST"
          host = "127.0.0.1"
          port = 3339
          idle-timeout-minutes = 12
        }

        cors {
          any-origin = true
          allow-credentials = false
          max-age-seconds = 120
          any-method = false
        }

        data {
          file = "lb.json"
          dir = "   "
        }
      """
    ).resolve()

    val config = LeaderboardAppConfig.load(testConfig)
    assert(config.service.name === "LEADERBOARD-TEST")
    assert(config.service.host === "127.0.0.1")
    assert(config.service.port === 3339)
    assert(config.service.idleTimeoutMinutes === 12)

    assert(config.cors.anyOrigin)
    assert(!config.cors.allowCredentials)
    assert(config.cors.maxAgeSeconds === 120L)
    assert(!config.cors.anyMethod)

    assert(config.data.file === "lb.json")
    assert(config.data.dataDir.isEmpty)
  }

  test("testLoadReadsDataDirectoryWhenProvided") {
    val testConfig = ConfigFactory.parseString(
      """
        service {
          name = "LEADERBOARD"
          host = "0.0.0.0"
          port = 3333
          idle-timeout-minutes = 30
        }

        cors {
          any-origin = true
          allow-credentials = true
          max-age-seconds = 86400
          any-method = true
        }

        data {
          file = "leaderboard.json"
          dir = "./data"
        }
      """
    ).resolve()

    val config = LeaderboardAppConfig.load(testConfig)
    assert(config.data.file === "leaderboard.json")
    assert(config.data.dataDir.contains("./data"))
  }
}
