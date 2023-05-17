package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.client._
import pl.smtc.smartwords.database._

import java.util.UUID

class QuizServiceTest extends AnyFunSuite {

  private val uuidRegex: String = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  test("testStartQuiz") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.startQuiz(Some(5), Some(1), Some("pl"))
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res.matches(uuidRegex))
  }

  test("testStartQuizFailsWhenWordServiceIsNotAlive") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest(false)
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.startQuiz(None, None, None)
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Cannot start quiz. Service: WORD - not available.")
  }

  test("testStartQuizFailsWhenWordServiceCannotProvideRandomWord") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest(wordFail = true)
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.startQuiz(None, None, None)
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Cannot start quiz: Invalid input parameter(s) - getRandomWord error!")
  }

  test("testStartQuizFailsWhenWordServiceCannotProvideWordsByCategory") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest(categoryFail = true)
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.startQuiz(None, None, None)
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Cannot start quiz: Invalid input parameter(s) - getWordsByCategory error!")
  }

  test("testGetQuizQuestionNo") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: Json = serviceUnderTest.getQuizQuestionNo(uuid, "1")
                                    .flatMap(_.as[Json])
                                    .unsafeRunSync()
    assert(res.hcursor.downField("word").as[String] match {
      case Right(s) => s.startsWith("word-es-72")
      case Left(_) => false
    })
    assert(res.hcursor.downField("options").as[Array[String]] match {
      case Right(s) => s.length == 4 && (s.head.startsWith("def") || s.head.startsWith("alt"))
      case Left(_) => false
    })
  }

  test("testGetQuizQuestionNoFailsWhenUnsupportedUuidIsProvided") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.getQuizQuestionNo(UUID.randomUUID(), "1")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Specified quiz does not exist")
  }

  test("testGetQuizQuestionNoFailsWhenQuestionNumberIsNotAnInteger") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.getQuizQuestionNo(uuid, "non-int")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Question number must be of integer type.")
  }

  test("testGetQuizQuestionNoFailsWhenQuestionNumberIsOutsideRange") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.getQuizQuestionNo(uuid, "7")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Question number must have value between 0-4")
  }

  test("testStopQuiz") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.stopQuiz(uuid)
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res.nonEmpty)
    assert(res.toDouble === 0.0)
  }

  test("testStopQuizFailsWhenUnsupportedUuidIsProvided") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.stopQuiz(UUID.randomUUID())
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Specified quiz does not exist")
  }

  test("testPostQuizQuestionNo") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.postQuizQuestionNo(uuid, "0", "2")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "true" || res === "false")
  }

  test("testPostQuizQuestionNoFailsWhenUnsupportedUuidIsProvided") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.postQuizQuestionNo(UUID.randomUUID(), "1", "0")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Specified quiz does not exist")
  }

  test("testPostQuizQuestionNoFailsWhenQuestionNumberIsNotAnInteger") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.postQuizQuestionNo(uuid, "non-integer", "0")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Question and answer number must be of integer type.")
  }

  test("testPostQuizQuestionNoFailsWhenQuestionNumberIsNotAValidInteger") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.postQuizQuestionNo(uuid, "5", "3")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Question number must have value between 0-4")
  }

  test("testPostQuizQuestionNoFailsWhenAnswerNumberIsNotAnInteger") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.postQuizQuestionNo(uuid, "0", "non-integer")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Question and answer number must be of integer type.")
  }

  test("testPostQuizQuestionNoFailsWhenAnswerNumberIsNotAValidInteger") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val uuid: UUID = UUID.fromString(serviceUnderTest.startQuiz(Some(5), Some(72), Some("es"))
                                                     .flatMap(_.as[String])
                                                     .unsafeRunSync())
    val res: String = serviceUnderTest.postQuizQuestionNo(uuid, "2", "4")
                                      .flatMap(_.as[String])
                                      .unsafeRunSync()
    assert(res === "Answer number must have value between 0-3")
  }
}
