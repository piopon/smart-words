package pl.smtc.smartwords.model

import org.scalatest.funsuite.AnyFunSuite

class DictionaryTest extends AnyFunSuite {

  test("testEmpty") {
    val result: Dictionary = Dictionary.empty()
    assert(result.file.isEmpty)
    assert(result.game.isEmpty)
    assert(result.mode === None)
    assert(result.language.isEmpty)
  }

  test("testCreate") {
    val result: Dictionary = Dictionary.create(Some(99), "pl")
    assert(result.file.startsWith("words-quiz-99-pl@"))
    assert(result.game === "quiz")
    assert(result.mode.nonEmpty)
    assert(result.mode.get === 99)
    assert(result.language === "pl")
  }

  test("testCreateWithoutMode") {
    val result: Dictionary = Dictionary.create(None, "en")
    assert(result.file.startsWith("words-quiz-en@"))
    assert(result.game === "quiz")
    assert(result.mode.isEmpty)
    assert(result.language === "en")
  }

  test("testFromFile") {
    val result: Dictionary = Dictionary.fromFile("words-TEST-13-pt@2023-05-26.json")
    assert(result.file === "words-TEST-13-pt@2023-05-26.json")
    assert(result.game === "TEST")
    assert(result.mode.nonEmpty)
    assert(result.mode.get === 13)
    assert(result.language === "pt")
  }

  test("testFromFileWithoutMode") {
    val result: Dictionary = Dictionary.fromFile("words-quiz-en@2023-05-26.json")
    assert(result.file === "words-quiz-en@2023-05-26.json")
    assert(result.game === "quiz")
    assert(result.mode.isEmpty)
    assert(result.language === "en")
  }

  test("testFromFileUsesDefaultsWhenPrefixIsNotWords") {
    val result: Dictionary = Dictionary.fromFile("dictionary-quiz-7-de@2023-05-26.json")
    assert(result.file === "dictionary-quiz-7-de@2023-05-26.json")
    assert(result.game === "quiz")
    assert(result.mode.isEmpty)
    assert(result.language === "pl")
  }

  test("testFromFileWithInvalidModeTokenKeepsModeEmpty") {
    val result: Dictionary = Dictionary.fromFile("words-quiz-invalid-en@2023-05-26.json")
    assert(result.file === "words-quiz-invalid-en@2023-05-26.json")
    assert(result.game === "quiz")
    assert(result.mode.isEmpty)
    assert(result.language === "en")
  }

  test("testFromFileWithTwoPartWordsPrefixUsesDefaults") {
    val result: Dictionary = Dictionary.fromFile("words-quiz@2023-05-26.json")
    assert(result.file === "words-quiz@2023-05-26.json")
    assert(result.game === "quiz")
    assert(result.mode.isEmpty)
    assert(result.language === "pl")
  }
}
