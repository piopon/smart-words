package pl.smtc.smartwords.middleware

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model._

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

  test("testValidateParameterModeReturnsOkWhenInputIsOk") {
    val middleware: WordMiddleware = new WordMiddleware()
    val result: Option[Int] = middleware.validateParameterMode("1", Some(List(1, 2)))
    assert(result.nonEmpty)
    assert(result.get === 1)
  }

  test("testValidateParameterModeThrowsWhenInputIsNok") {
    val middleware: WordMiddleware = new WordMiddleware()
    assertThrows[WordMiddlewareException](middleware.validateParameterMode("5", Some(List(1, 2))))
  }

  test("testValidateParameterModeThrowsWhenInputIsInvalid") {
    val middleware: WordMiddleware = new WordMiddleware()
    assertThrows[WordMiddlewareException](middleware.validateParameterMode("abc", Some(List(1, 2))))
  }

  test("testValidateParameterCategoryReturnsOkWhenInputIsOk") {
    val middleware: WordMiddleware = new WordMiddleware()
    val result: Option[Category.Value] = middleware.validateParameterCategory(Some("verb"))
    assert(result.nonEmpty)
    assert(result.get === Category.verb)
  }

  test("testValidateParameterCategoryReturnsNoneWhenInputIsNone") {
    val middleware: WordMiddleware = new WordMiddleware()
    val result: Option[Category.Value] = middleware.validateParameterCategory(None)
    assert(result.isEmpty)
  }
}
