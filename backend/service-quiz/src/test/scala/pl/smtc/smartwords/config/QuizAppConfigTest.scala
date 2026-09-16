package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite

class QuizAppConfigTest extends AnyFunSuite {

  test("testLoadReadsConfiguredValuesAndNormalizesOptionalPaths") {
    val testConfig = ConfigFactory.parseResources("quiz-app-config-test.conf").resolve()
    val config = QuizAppConfig.load(testConfig)

    assert(config.service.name === "QUIZ-TEST")
    assert(config.service.host === "127.0.0.2")
    assert(config.service.port === 3222)
    assert(config.service.idleTimeoutMinutes === 12)

    assert(!config.cors.anyOrigin)
    assert(config.cors.allowCredentials)
    assert(config.cors.maxAgeSeconds === 60L)
    assert(!config.cors.anyMethod)

    assert(config.data.modeFile === "modes-test.json")
    assert(config.data.dataDir.isEmpty)
    assert(config.data.seedDir.isEmpty)

    assert(config.wordService.name === "WORD-TEST")
    assert(config.wordService.host === "word-service-test")
    assert(config.wordService.port === 4111)
    assert(config.wordService.baseUrl === "http://word-service-test:4111")
    assert(config.wordService.requestTimeoutSeconds === 2.0)

    assert(config.quiz.defaultSize === 7)
    assert(config.quiz.defaultMode === 3)
    assert(config.quiz.defaultLanguage === "en")
  }

  test("testLoadReadsOptionalDataPathsWhenProvided") {
    val testConfig = ConfigFactory.parseString(
      """
        service {
          name = "QUIZ-TEST"
          host = "127.0.0.2"
          port = 3222
          idle-timeout-minutes = 12
        }

        cors {
          any-origin = false
          allow-credentials = true
          max-age-seconds = 60
          any-method = false
        }

        data {
          mode-file = "modes-test.json"
          dir = "./quiz-data"
          seed-dir = "./seed-data"
        }

        word-service {
          name = "WORD-TEST"
          host = "word-service-test"
          port = 4111
          base-url = "http://word-service-test:4111"
          request-timeout-seconds = 2.0
        }

        quiz {
          default-size = 7
          default-mode = 3
          default-language = "en"
        }
      """
    ).resolve()

    val config = QuizAppConfig.load(testConfig)
    assert(config.data.dataDir.contains("./quiz-data"))
    assert(config.data.seedDir.contains("./seed-data"))
  }
}