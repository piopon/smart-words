package pl.smtc.smartwords.middleware

import org.scalatest.funsuite.AnyFunSuite

class WordMiddlewareTest extends AnyFunSuite {

  test("testValidateParameterLanguageReturnsOkWhenInputIsOk") {
    val middleware: WordMiddleware = new WordMiddleware()
    val result: String = middleware.validateParameterLanguage("pl", List("pl", "en", "de"))
    assert(result === "pl")
  }

  test("testValidateParameterLanguageThrowsWhenInputIsNok") {
    val middleware: WordMiddleware = new WordMiddleware()
    assertThrows[WordMiddlewareException](middleware.validateParameterLanguage("pl", List("en", "de")))
  }

}
