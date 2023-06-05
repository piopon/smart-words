package pl.smtc.smartwords.utilities

import org.scalatest.funsuite.AnyFunSuite

class DataParserTest extends AnyFunSuite {

  test("testParseGameModeReturnsCorrectResultWhenDataIsCorrect") {
    val parserUnderTest: DataParser = new DataParser()
    val result: Option[Int] = parserUnderTest.parseGameMode("11")
    assert(result.nonEmpty)
    assert(result.get === 11)
  }
}
