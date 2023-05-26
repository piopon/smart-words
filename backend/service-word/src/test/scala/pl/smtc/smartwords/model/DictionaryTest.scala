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
    assert(result.file === "words-quiz-99-pl@2023-05-26.json")
    assert(result.game === "quiz")
    assert(result.mode.nonEmpty)
    assert(result.mode.get === 99)
    assert(result.language === "pl")
  }

  test("testFromFile") {
    val result: Dictionary = Dictionary.fromFile("words-TEST-13-pt@2023-05-26.json")
    assert(result.file === "words-TEST-13-pt@2023-05-26.json")
    assert(result.game === "TEST")
    assert(result.mode.nonEmpty)
    assert(result.mode.get === 13)
    assert(result.language === "pt")
  }
}
