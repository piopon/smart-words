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

  /**
   * Method used to start a new quiz
   * @param maybeSize an optional size value (if none then default value 10 will be applied)
   * @return response with appropriate status
   */
  def startQuiz(maybeSize: Option[Int]): IO[Response[IO]] = {
    val size: Int = maybeSize match {
      case None => 10
      case Some(size) => size
    }
    Ok(quizDB.addQuiz(generateQuiz(size)).toString)
  }

  /**
   * Method used to retrieve a specified question number from previously started quiz
   * @param quizId the UUID of a started quiz (received after sending start quiz request)
   * @param questionNo question number from a specified quiz
   * @return response with desired question number or not found status if quiz UUID not present
   */
  def getQuizQuestionNo(quizId: UUID, questionNo: String): IO[Response[IO]] = {
    quizDB.getQuiz(quizId) match {
      case None =>
        NotFound("Specified quiz does not exist")
      case Some(quiz) =>
        Ok(quiz.rounds(questionNo.toInt).asJson)
    }
  }

  /**
   * Method used to post an answer for specified quiz question number
   * @param quizId the UUID of a started quiz (received after sending start quiz request)
   * @param questionNo question number from a specified quiz
   * @param answerNo answer number for a specified question number
   * @return response with desired answer correct status or not found status if quiz UUID not present
   */
  def postQuizQuestionNo(quizId: UUID, questionNo: String, answerNo: String): IO[Response[IO]] = {
    quizDB.getQuiz(quizId) match {
      case None =>
        NotFound("Specified quiz does not exist")
      case Some(quiz) =>
        val correctDefinition: String = quiz.rounds(questionNo.toInt).word.definition
        val selectedDefinition: String = quiz.rounds(questionNo.toInt).options(answerNo.toInt)
        quiz.rounds(questionNo.toInt).answer = Option(answerNo.toInt)
        val isCorrect = correctDefinition.equals(selectedDefinition)
        quiz.rounds(questionNo.toInt).correct = Option(isCorrect)
        Ok(isCorrect.toString)
    }
  }

  /**
   * Method used to stop desired quiz
   * @param quizId the UUID of a started quiz (received after sending start quiz request)
   * @return response with correct percentage or not found status if quiz UUID not present
   */
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

  /**
   * Method used to generate a new round object
   * @return generated round object with random word and 4 answer options
   */
  private def generateRound(forbiddenWords: List[String] = List.empty): Round = {
    var word: Word = null
    do {
      word = wordDB.getWord(Random.nextInt(wordDB.getWords.length)).get
    } while (forbiddenWords.contains(word.name))
    Round(word, generateOptions(word.definition, word.category), None, None)
  }

  /**
   * Method used to generate specified number of rounds
   * @param size desired number of rounds to be generated
   * @return list of specified number of rounds
   */
  private def generateRounds(size: Int): List[Round] = {
    var rounds: List[Round] = List.fill(size)(generateRound())
    rounds = rounds.distinctBy(_.word.name)
    for (_ <- 0 until size-rounds.length) {
      val replacement: Round = generateRound(rounds.map(r => r.word.name))
      rounds = rounds.appended(replacement)
    }
    rounds
  }

  /**
   * Method used to generate 4 answer options
   * @param correctDefinition correct word definition (one of answer options)
   * @param category word category from which to draw the remaining 3 answer options
   * @return list of possible 4 answer options
   */
  private def generateOptions(correctDefinition: String, category: Category.Value): List[String] = {
    val incorrectOptions: List[String] = Random.shuffle(wordDB.getWordsByCategory(category).map(_.definition))
    val options: List[String] = incorrectOptions.take(3) :+ correctDefinition
    Random.shuffle(options)
  }

  /**
   * Method used to generate a new quiz object
   * @param size desired size of quiz (number of questions)
   * @return generated quiz object
   */
  private def generateQuiz(size: Int): Quiz = {
    Quiz(generateRounds(size), 0)
  }
}
