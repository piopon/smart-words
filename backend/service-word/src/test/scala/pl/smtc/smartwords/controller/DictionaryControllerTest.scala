package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe._
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

import java.io.File
import java.nio.file._

class DictionaryControllerTest extends AnyFunSuite with BeforeAndAfterAll {

  private val serviceTestFile: String = "dictionary-controller-test.json"

  override def afterAll(): Unit = {
    for {
      files <- Option(new File(Paths.get(getClass.getResource("/").toURI).toString).listFiles)
      file <- files if file.getName.endsWith(".json") && !file.getName.endsWith("words-quiz-1-pl@dictionary.json")
    } file.delete()
  }

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val controllerUnderTest: DictionaryController = new DictionaryController(createTestDatabase())
    val endpoint: String = s"/non-existing"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

  test("testGetRoutesReturnsOkStatusForGettingDictionaries") {
    val controllerUnderTest: DictionaryController = new DictionaryController(createTestDatabase())
    val endpoint: String = s"/"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json = json"""[ { "game" : "quiz", "mode" : 998, "language" : "en" },
                                  { "game" : "quiz", "mode" : 999, "language" : "pl" },
                                  { "game" : "quiz", "mode" : 997, "language" : "de" } ]"""
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
