package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.Quiz

import java.util.UUID

class QuizDatabaseTest extends AnyFunSuite {

  test("testAddQuiz") {
    val databaseUnderTest: QuizDatabase = new QuizDatabase()
    val result: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    assert(result.toString.nonEmpty)
  }

  test("testRandomUUID") {
    val databaseUnderTest: QuizDatabase = new QuizDatabase()
    val result1: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    val result2: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    assert(result1.toString !== result2.toString)
  }

  test("testRemoveQuiz") {
    val databaseUnderTest: QuizDatabase = new QuizDatabase()
    val uuid: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    val result: Option[Quiz] = databaseUnderTest.removeQuiz(uuid)
    assert(result.nonEmpty)
  }

  test("testRemoveEmpty") {
    val databaseUnderTest: QuizDatabase = new QuizDatabase()
    val uuid: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    val randomUuid: UUID = UUID.randomUUID()
    assert(uuid.toString !== randomUuid.toString)
    val result: Option[Quiz] = databaseUnderTest.removeQuiz(randomUuid)
    assert(result.isEmpty)
  }

  test("testGetQuiz") {
    val databaseUnderTest: QuizDatabase = new QuizDatabase()
    val uuid: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    val result: Option[Quiz] = databaseUnderTest.getQuiz(uuid)
    assert(result.nonEmpty)
    assert(result.get.rounds.isEmpty)
    assert(result.get.score === 0)
  }

  test("testGetEmpty") {
    val databaseUnderTest: QuizDatabase = new QuizDatabase()
    val uuid: UUID = databaseUnderTest.addQuiz(Quiz(List(), 0))
    val randomUuid: UUID = UUID.randomUUID()
    assert(uuid.toString !== randomUuid.toString)
    val result: Option[Quiz] = databaseUnderTest.getQuiz(randomUuid)
    assert(result.isEmpty)
  }
}
