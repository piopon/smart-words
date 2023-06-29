package pl.smtc.smartwords.utilities

import org.scalatest.funsuite.AnyFunSuite

class DataParserTest extends AnyFunSuite {

  test("testParseGameModeReturnsCorrectResultWhenDataIsCorrect") {
    val parserUnderTest: DataParser = new DataParser()
    val result: Option[Int] = parserUnderTest.parseGameMode("11")
    assert(result.nonEmpty)
    assert(result.get === 11)
  }

  test("testParseGameModeReturnsNoneWhenDataIsEmpty") {
    val parserUnderTest: DataParser = new DataParser()
    val result: Option[Int] = parserUnderTest.parseGameMode("")
    assert(result.isEmpty)
  }

  test("testParseGameModeReturnsNoneWhenDataIsIncorrect") {
    val parserUnderTest: DataParser = new DataParser()
    val result: Option[Int] = parserUnderTest.parseGameMode("non-integer")
    assert(result.isEmpty)
  }
}
