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
}