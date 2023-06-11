package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s.circe.jsonDecoder
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

import java.io.File
import java.nio.file.Paths

class DictionaryServiceTest extends AnyFunSuite with BeforeAndAfterAll {

  private val serviceTestFile: String = "dictionary-service-test.json"

  test("testGetDictionaries") {
    val serviceUnderTest: DictionaryService = new DictionaryService(createTestDatabase())
    val res: Json = serviceUnderTest.getDictionaries.flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[ { "game" : "quiz", "mode" : 998, "language" : "en" },
                                  { "game" : "quiz", "mode" : 999, "language" : "pl" },
                                  { "game" : "quiz", "mode" : 997, "language" : "de" } ]"""
    assert(res === expected)
  }

  private def createTestDatabase(): WordDatabase = {
    val database: WordDatabase = new WordDatabase()
    val dictionaryPl: Dictionary = Dictionary(serviceTestFile, "quiz", Some(999), "pl")
    val dictionaryEn: Dictionary = Dictionary(serviceTestFile, "quiz", Some(998), "en")
    val dictionaryDe: Dictionary = Dictionary(serviceTestFile, "quiz", Some(997), "de")
    database.addWord(Word("word-1-pl", Category.verb, List(""), dictionaryPl))
    database.addWord(Word("word-2-pl", Category.latin, List(""), dictionaryPl))
    database.addWord(Word("word-1-en", Category.adjective, List(""), dictionaryEn))
    database.addWord(Word("word-3-pl", Category.latin, List(""), dictionaryPl))
    database.addWord(Word("word-2-en", Category.person, List(""), dictionaryEn))
    database.addWord(Word("word-1-de", Category.noun, List(""), dictionaryDe))
    database
  }
}
