package pl.smtc.smartwords.dao

import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model._

class QuizDaoTest extends AnyFunSuite {

  private val testWordName: String = "new-test-word"
  private val testRoundOptions: Iterable[String] = List("option-1", "option-II", "option-C", "option-d")

  test("testGetRoundEncoder") {
    val encoderUnderTest: Encoder[Round] = QuizDao.getRoundEncoder
    val sourceWord: Word = Word(testWordName, "test-cat", List("definition", "alt-definition"))
    val sourceRound: Round = Round(sourceWord , testRoundOptions.toList, Some(false), Some(3))
    val encodedValue: Json = encoderUnderTest.apply(sourceRound)
    val expectedValue: Json = createRoundJson(testWordName)
    assert(encodedValue === expectedValue)
  }

  /**
   * Method used to create quiz round JSON object with hardcoded options, answer, correct flag, and customizable word
   * @param word word name which is used in the quiz round
   * @return JSON object representing quiz round
   */
  private def createRoundJson(word: String): Json = {
    Json.obj(
      ("word", Json.fromString(word)),
      ("options", Json.fromValues(testRoundOptions.map(option => Json.fromString(option)))),
      ("correct", Json.fromBoolean(false)),
      ("answer", Json.fromInt(3))
    )
  }
}
