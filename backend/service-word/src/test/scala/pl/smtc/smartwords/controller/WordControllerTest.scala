package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe.{jsonDecoder, jsonEncoder}
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

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithNonExistingMode") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/111/pl"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Invalid 'mode' parameter: value '111' is not supported.")
  }

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithNonExistingLanguage") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/es"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Invalid 'language' parameter: value 'es' is not supported.")
  }

  test("testGetRoutesReturnsEmptyResultWhenGettingWordsWithExistingButIncompatibleModeAndLanguage") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/997/pl"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsWithExistingModeAndLanguage") {
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

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsWithCategoryFilter") {
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

  test("testGetRoutesReturnsEmptyResponseWhenGettingWordsWithNotUsedCategoryFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?cat=noun"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithNotSupportedCategoryFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?cat=non-supported"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Invalid 'cat' parameter: value 'non-supported' is not supported.")
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsWithSizeFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=1"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-1-pl", "category" : "verb", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsWithBigSizeFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=1000000"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-1-pl", "category" : "verb", "description" : [""] },
                                  { "name" : "word-2-pl", "category" : "latin", "description" : [""] },
                                  { "name" : "word-3-pl", "category" : "latin", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithSizeFilterEqualsZero") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=0"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Incorrect 'size' parameter value: must be greater then 0.")
  }

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithNegativeSizeFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=-1"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Incorrect 'size' parameter value: must be greater then 0.")
  }

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithNonIntegerSizeFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=one"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Query decoding Int failed: invalid 'size' parameter value.")
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsWithCategoryAndSizeFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?cat=latin&size=1"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-2-pl", "category" : "latin", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsWithSizeAndCategoryFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/999/pl?size=1&cat=latin"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-2-pl", "category" : "latin", "description" : [""] } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsRandomFilterOff") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/998/en?random=false"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "name" : "word-1-en", "category" : "adjective", "description" : [""] },
                                  { "name" : "word-2-en", "category" : "person", "description" : [""] }]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsCorrectResponseWhenGettingWordsRandomFilterOn") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/998/en?random=true"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val sequence1: Json = json"""[ { "name" : "word-1-en", "category" : "adjective", "description" : [""] },
                                   { "name" : "word-2-en", "category" : "person", "description" : [""] }]"""
    val sequence2: Json = json"""[ { "name" : "word-2-en", "category" : "person", "description" : [""] },
                                   { "name" : "word-1-en", "category" : "adjective", "description" : [""] }]"""
    val actual: Json = response.get.as[Json].unsafeRunSync
    assert(actual === sequence1 || actual === sequence2)
  }

  test("testGetRoutesReturnsBadRequestWhenGettingWordsWithNonBooleanRandomFilter") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/998/en?random=0"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Query decoding Boolean failed: invalid 'random' parameter value.")
  }

  test("testGetRoutesReturnsCorrectResponseWhenAddingNewWord") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/997/de"
    val requestBody: Json = json"""{ "name" : "word-10-de", "category" : "person", "description" : [""] }"""
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    assert(response.get.as[String].unsafeRunSync === "added word 'word-10-de'")
  }

  test("testGetRoutesReturnsFoundWhenAddingExistingWord") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/997/de"
    val requestBody: Json = json"""{ "name" : "word-1-de", "category" : "person", "description" : [""] }"""
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Found)
    assert(response.get.as[String].unsafeRunSync === "word 'word-1-de' already defined")
  }

  test("testGetRoutesReturnsBadRequestWhenAddingNewWordWithBadLanguage") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/997/it"
    val requestBody: Json = json"""{ "name" : "word-10-de", "category" : "person", "description" : [""] }"""
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Invalid 'language' parameter: value 'it' is not supported.")
  }

  test("testGetRoutesReturnsBadRequestWhenAddingNewWordWithInvalidMode") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/23/de"
    val requestBody: Json = json"""{ "name" : "word-1-de", "category" : "person", "description" : [""] }"""
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Invalid 'mode' parameter: value '23' is not supported.")
  }

  test("testGetRoutesReturnsBadRequestWhenAddingNewWordWithNonIntegerMode") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/mode/de"
    val requestBody: Json = json"""{ "name" : "word-1-de", "category" : "person", "description" : [""] }"""
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    assert(response.get.as[String].unsafeRunSync === "Invalid 'mode' parameter: value 'mode' cannot be parsed.")
  }

  test("testGetRoutesReturnsCorrectResponseWhenUpdatingExistingWord") {
    val controllerUnderTest: WordController = new WordController(createTestDatabase())
    val endpoint: String = s"/997/de/word-1-de"
    val requestBody: Json = json"""{ "name" : "word-10-de", "category" : "person", "description" : [""] }"""
    val request: Request[IO] = Request(Method.PUT, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    assert(response.get.as[String].unsafeRunSync === "updated word 'word-1-de'")
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
