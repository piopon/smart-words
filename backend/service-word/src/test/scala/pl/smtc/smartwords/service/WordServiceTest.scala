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

  test("testGetWordsReturnsEmptyResultWhenNoneModeIsSelected") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(None, "pl", Some(Category.verb), Some(10), Some(false))
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsEmptyResultWhenNotUsedModeIsSelected") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(995), "pl", Some(Category.verb), Some(10), Some(false))
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsCorrectResultWhenLanguageFilterIsApplied") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(997), "de", None, None, None)
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{ "name" : "word-1-de", "category" : "noun", "description" : [ "" ] }]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsEmptyResultWhenEmptyLanguageIsSelected") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(999), "", Some(Category.verb), Some(10), Some(false))
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsEmptyResultWhenNotUsedLanguageFilterIsApplied") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(999), "fr", Some(Category.verb), Some(10), Some(false))
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsCorrectResultWhenCategoryFilterIsNotUsed") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(999), "pl", None, None, None)
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{ "name" : "word-1-pl", "category" : "verb", "description" : [ "" ] },
                                 { "name" : "word-2-pl", "category" : "latin", "description" : [ "" ] },
                                 { "name" : "word-3-pl", "category" : "latin", "description" : [ "" ] }]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsCorrectResultWhenCategoryFilterIsApplied") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(999), "pl", Some(Category.verb), None, None)
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{ "name" : "word-1-pl", "category" : "verb", "description" : [ "" ] }]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsEmptyResultWhenNotUsedCategoryFilterIsApplied") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(999), "pl", Some(Category.adjective), None, None)
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[]"""
    assert(res === expected)
  }

  test("testGetWordsReturnsCorrectResultWhenSizeFilterIsApplied") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val res: Json = serviceUnderTest.getWords(Some(998), "en", None, Some(1), None)
                                    .flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{ "name" : "word-1-en", "category" : "adjective", "description" : [ "" ] }]"""
    assert(res === expected)
  }

  test("testAddWordReturnsCorrectResultWhenAddingNewWord") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val word: Word = Word("word-1-fr", Category.noun, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.addWord(Some(1), "fr", word)
                                      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "added word 'word-1-fr'")
  }

  test("testAddWordReturnsErrorWhenAddedWordHasTheSameNameModeAndLang") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val word: Word = Word("word-1-pl", Category.verb, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.addWord(Some(999), "pl", word)
                                      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "word 'word-1-pl' already defined")
  }

  test("testAddWordReturnsCorrectResultWhenAddedWordHasTheSameNameModeButDifferentLang") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val word: Word = Word("word-1-pl", Category.verb, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.addWord(Some(999), "it", word)
      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "added word 'word-1-pl'")
  }

  test("testAddWordReturnsCorrectResultWhenAddedWordHasTheSameNameLangButDifferentMode") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val word: Word = Word("word-1-pl", Category.verb, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.addWord(Some(1), "pl", word)
      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "added word 'word-1-pl'")
  }

  test("testAddWordReturnsCorrectResultWhenAddedWordHasTheSameModeLangButDifferentName") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val word: Word = Word("word-10-pl", Category.verb, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.addWord(Some(999), "pl", word)
      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "added word 'word-10-pl'")
  }

  test("testUpdateWordReturnsCorrectResultWhenUpdatingAnExistingWord") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val newWord: Word = Word("updated-1-pl", Category.verb, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.updateWord(Some(999), "pl", "word-1-pl", newWord)
                                      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "updated word 'word-1-pl'")
  }

  test("testUpdateWordReturnsErrorWhenUpdatingNotExistingWord") {
    val serviceUnderTest: WordService = new WordService(createTestDatabase())
    val newWord: Word = Word("updated-1-pl", Category.verb, List("def-1"), Dictionary.empty())
    val res: String = serviceUnderTest.updateWord(Some(11), "fr", "word-1-fr", newWord)
                                      .flatMap(_.as[String]).unsafeRunSync()
    assert(res === "word 'word-1-fr' not found in DB")
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