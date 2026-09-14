package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite

class WordAppConfigTest extends AnyFunSuite {

  test("testLoadReadsConfiguredValuesAndNormalizesOptionalPaths") {
    System.setProperty("config.resource", "word-app-config-test.conf")
    ConfigFactory.invalidateCaches()

    try {
      val config = WordAppConfig.load()

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
    } finally {
      System.clearProperty("config.resource")
      ConfigFactory.invalidateCaches()
    }
  }
}