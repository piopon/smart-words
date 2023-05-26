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
}
