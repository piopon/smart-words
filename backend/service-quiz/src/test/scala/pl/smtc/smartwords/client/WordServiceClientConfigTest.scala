package pl.smtc.smartwords.client

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.config.QuizWordServiceConfig

class WordServiceClientConfigTest extends AnyFunSuite {

  test("testUsesConfiguredHostAndPortWhenBaseUrlIsEmpty") {
    val config = QuizWordServiceConfig(
      name = "WORD",
      host = "service-word",
      port = 19191,
      baseUrl = "",
      requestTimeoutSeconds = 1.0
    )

    val serviceUnderTest = new WordService(Some(config))
    assert(serviceUnderTest.address.toString === "http://service-word:19191")
    assert(serviceUnderTest.healthEndpoint.toString.endsWith("/health"))
    assert(serviceUnderTest.wordsEndpoint.toString.endsWith("/words"))
  }

  test("testUsesConfiguredBaseUrlWhenProvided") {
    val config = QuizWordServiceConfig(
      name = "WORD",
      host = "service-word",
      port = 19191,
      baseUrl = "http://custom-host:18181",
      requestTimeoutSeconds = 2.5
    )

    val serviceUnderTest = new WordService(Some(config))
    assert(serviceUnderTest.address.toString === "http://custom-host:18181")
  }

  test("testFallsBackToConfiguredHostAndPortWhenBaseUrlIsInvalid") {
    val config = QuizWordServiceConfig(
      name = "WORD",
      host = "fallback-host",
      port = 17171,
      baseUrl = "://invalid-url",
      requestTimeoutSeconds = 1.0
    )

    val serviceUnderTest = new WordService(Some(config))
    assert(serviceUnderTest.address.toString === "http://fallback-host:17171")
  }

  test("testInitializesWithContainerDefaultBranchWhenDetectorReturnsTrue") {
    val config = QuizWordServiceConfig(
      name = "WORD",
      host = "configured-host",
      port = 19191,
      baseUrl = "",
      requestTimeoutSeconds = 1.0
    )

    val serviceUnderTest = new WordService(Some(config), () => true)
    assert(serviceUnderTest.address.toString === "http://configured-host:19191")
  }

  test("testUsesResolvedDefaultsWhenConfigIsNotProvided") {
    val serviceUnderTest = new WordService(None, () => false)

    assert(serviceUnderTest.address.toString.startsWith("http://"))
    assert(serviceUnderTest.healthEndpoint.toString.endsWith("/health"))
    assert(serviceUnderTest.wordsEndpoint.toString.endsWith("/words"))
  }
}
