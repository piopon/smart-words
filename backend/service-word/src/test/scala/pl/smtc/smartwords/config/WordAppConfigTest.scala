package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite

class WordAppConfigTest extends AnyFunSuite {

  test("testLoadReadsConfiguredValuesAndNormalizesOptionalPaths") {
    val testConfig = ConfigFactory.parseResources("word-app-config-test.conf").resolve()
    val config = WordAppConfig.load(testConfig)

    assert(config.service.name === "WORD-TEST")
    assert(config.service.host === "127.0.0.1")
    assert(config.service.port === 3111)
    assert(config.service.idleTimeoutMinutes === 15)

    assert(config.cors.anyOrigin)
    assert(!config.cors.allowCredentials)
    assert(config.cors.maxAgeSeconds === 120L)
    assert(!config.cors.anyMethod)

    assert(config.data.dictionaryExtension === "JSON")
    assert(config.data.dataDir.isEmpty)
    assert(config.data.seedDir.isEmpty)
  }

  test("testLoadReadsOptionalPathsWhenProvided") {
    val testConfig = ConfigFactory.parseString(
      """
        service {
          name = "WORD-TEST"
          host = "127.0.0.1"
          port = 3111
          idle-timeout-minutes = 15
        }

        cors {
          any-origin = true
          allow-credentials = false
          max-age-seconds = 120
          any-method = false
        }

        data {
          dictionary-extension = "JSON"
          dir = "./data"
          seed-dir = "./seed"
        }
      """
    ).resolve()

    val config = WordAppConfig.load(testConfig)
    assert(config.data.dataDir.contains("./data"))
    assert(config.data.seedDir.contains("./seed"))
  }
}