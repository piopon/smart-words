package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class WordServiceTest extends AnyFunSuite {

  private val serviceTestFile: String = "word-service-test.json"

  test("testGetWordsReturnsEmptyResultWhenNotExistingModeIsSelected") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(None, "pl", None, None, None).flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[]"""
    assert(res === expected)
  }

  private def createTestDatabase(): WordDatabase = {
    val database: WordDatabase = new WordDatabase()
    val dictionaryPl: Dictionary = Dictionary(serviceTestFile, "quiz", Some(999), "pl")
    val dictionaryEn: Dictionary = Dictionary(serviceTestFile, "quiz", Some(998), "en")
    val dictionaryDe: Dictionary = Dictionary(serviceTestFile, "quiz", Some(997), "de")
    database.addWord(Word("word-1-pl", Category.verb, List(""), dictionaryPl))
    database.addWord(Word("word-2-pl", Category.verb, List(""), dictionaryPl))
    database.addWord(Word("word-1-en", Category.verb, List(""), dictionaryEn))
    database.addWord(Word("word-3-pl", Category.verb, List(""), dictionaryPl))
    database.addWord(Word("word-2-en", Category.verb, List(""), dictionaryEn))
    database.addWord(Word("word-1-de", Category.verb, List(""), dictionaryDe))
    database
  }
}
