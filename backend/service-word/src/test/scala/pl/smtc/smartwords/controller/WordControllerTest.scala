package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class WordControllerTest extends AnyFunSuite {

  private val serviceTestFile: String = "word-controller-test.json"

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/non-existing"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

  test("testGetRoutesReturnsCorrectResponseWhenAskingForSpecificWords") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-1-pl", "category" : "verb", "description" : [""] },
                                  { "name" : "word-2-pl", "category" : "latin", "description" : [""] },
                                  { "name" : "word-3-pl", "category" : "latin", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenAskingForSpecificWordsWithCategoryFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?cat=latin"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-2-pl", "category" : "latin", "description" : [""] },
                                  { "name" : "word-3-pl", "category" : "latin", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenAskingForSpecificWordsWithSizeFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=1"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-1-pl", "category" : "verb", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
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
