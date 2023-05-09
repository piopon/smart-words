package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.client.WordServiceTest
import pl.smtc.smartwords.database.QuizDatabase
import pl.smtc.smartwords.model.Round

import java.util.UUID

class QuizServiceTest extends AnyFunSuite {

  private val uuidRegex: String ="^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  test("testStartQuiz") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.startQuiz(Some(5), Some(1), Some("pl")).flatMap(_.as[String]).unsafeRunSync()
    assert(res.matches(uuidRegex))
  }

  test("testGetQuizQuestionNo") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val start: String = serviceUnderTest.startQuiz(Some(5), Some(72), Some("es")).flatMap(_.as[String]).unsafeRunSync()
    val uuid: UUID = UUID.fromString(start)
    val res: Json = serviceUnderTest.getQuizQuestionNo(uuid, "1").flatMap(_.as[Json]).unsafeRunSync()
    assert(res.hcursor.downField("word").as[String] match {
      case Right(s) => s.startsWith("word-es-72")
      case Left(_) => false
    })
    assert(res.hcursor.downField("options").as[Array[String]] match {
      case Right(s) => s.length == 4 && (s.head.startsWith("def") || s.head.startsWith("alt"))
      case Left(_) => false
    })
  }

  test("testStopQuiz") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val start: String = serviceUnderTest.startQuiz(Some(5), Some(72), Some("es")).flatMap(_.as[String]).unsafeRunSync()
    val uuid: UUID = UUID.fromString(start)
    val res: String = serviceUnderTest.stopQuiz(uuid).flatMap(_.as[String]).unsafeRunSync()
    assert(res.nonEmpty)
    assert(res.toDouble === 0.0)
  }
}
