package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.client.WordServiceTest
import pl.smtc.smartwords.database.QuizDatabase

class QuizServiceTest extends AnyFunSuite {

  private val uuidRegex: String ="^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  test("testStartQuiz") {
    val quizDatabase: QuizDatabase = new QuizDatabase
    val wordService: WordServiceTest = new WordServiceTest
    val serviceUnderTest: QuizService = new QuizService(quizDatabase, wordService)
    val res: String = serviceUnderTest.startQuiz(Some(5), Some(1), Some("pl")).flatMap(_.as[String]).unsafeRunSync()
    assert(res.matches(uuidRegex))
  }
}
