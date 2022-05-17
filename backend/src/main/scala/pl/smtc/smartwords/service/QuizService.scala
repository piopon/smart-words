package pl.smtc.smartwords.service

import cats.effect._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.dao._

import java.util.UUID
import scala.util.Random

class QuizService(quizDB: QuizDatabase, wordDB: WordDatabase) {

  implicit val RoundEncoder: Encoder[Round] = QuizDao.getRoundEncoder

  def startQuiz(maybeSize: Option[Int]): IO[Response[IO]] = {
    val size: Int = maybeSize match {
      case None => 10
      case Some(size) => size
    }
    Ok(quizDB.addQuiz(generateQuiz(size)).toString)
  }

  def getQuizQuestionNo(quizId: UUID, questionNo: String): IO[Response[IO]] = {
    quizDB.getQuiz(quizId) match {
      case None =>
        NotFound("Specified quiz does not exist")
      case Some(quiz) =>
        Ok(quiz.rounds(questionNo.toInt).asJson)
    }
  }

  def postQuizQuestionNo(quizId: UUID, questionNo: String, answerNo: String): IO[Response[IO]] = {
    quizDB.getQuiz(quizId) match {
      case None =>
        NotFound("Specified quiz does not exist")
      case Some(quiz) =>
        val correctDefinition: String = quiz.rounds(questionNo.toInt).word.definition
        val selectedDefinition: String = quiz.rounds(questionNo.toInt).options(answerNo.toInt)
        val isCorrect = correctDefinition.equals(selectedDefinition)
        quiz.rounds(questionNo.toInt).correct = Option(isCorrect)
        Ok(isCorrect.toString)
    }
  }

  def stopQuiz(quizId: UUID): IO[Response[IO]] = {
    quizDB.getQuiz(quizId) match {
      case None =>
        NotFound("Specified quiz does not exist")
      case Some(quiz) =>
        val okCount: Int = quiz.rounds.count(round => round.correct.exists(isCorrect => isCorrect))
        val percent: Float = okCount.toFloat / quiz.rounds.length
        quizDB.removeQuiz(quizId)
        Ok(percent.toString)
    }
  }

  private def generateRound(): Round = {
    val word: Word = wordDB.getWord(Random.nextInt(wordDB.getWords.length)).get
    Round(word, generateOptions(word.definition, word.category), None)
  }

  private def generateOptions(correctDefinition: String, category: Category.Value): List[String] = {
    val incorrectOptions: List[String] = Random.shuffle(wordDB.getWordsByCategory(category).map(_.definition))
    val options: List[String] = incorrectOptions.take(3) :+ correctDefinition
    Random.shuffle(options)
  }

  private def generateQuiz(size: Int): Quiz = {
    Quiz(List.fill(size)(generateRound()), 0)
  }
}
