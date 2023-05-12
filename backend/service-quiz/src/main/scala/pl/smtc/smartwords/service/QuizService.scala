package pl.smtc.smartwords.service

import cats.effect._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.client._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.dao._

import java.util.UUID
import scala.util.Random

class QuizService(quizDB: QuizDatabase, wordService: IWordService) {

  implicit val RoundEncoder: Encoder[Round] = QuizDao.getRoundEncoder

  private final val defaultQuizSize: Int = 10
  private final val defaultQuizMode: Int = 0
  private final val defaultQuizLang: String = "pl"

  /**
   * Method used to start a new quiz
   * @param maybeSize an optional size value (if none then default value will be applied)
   * @param maybeMode an optional mode value (if none then default value will be applied)
   * @param maybeLanguage an optional language value (if non then the default value will be applied)
   * @return response with appropriate status
   */
  def startQuiz(maybeSize: Option[Int], maybeMode: Option[Int], maybeLanguage: Option[String]): IO[Response[IO]] = {
    val size: Int = maybeSize match {
      case None => defaultQuizSize
      case Some(size) => size
    }
    val mode: Int = maybeMode match {
      case None => defaultQuizMode
      case Some(mode) => mode
    }
    val language: String = maybeLanguage match {
      case None => defaultQuizLang
      case Some(language) => language
    }
    if (wordService.isAlive) {
      try {
        Ok(quizDB.addQuiz(generateQuiz(size, mode, language)).toString)
      } catch {
        case e: QuizServiceException => BadRequest("Cannot start quiz: " + e.getMessage)
      }
    } else {
      ServiceUnavailable("Cannot start quiz. Service: WORD - not available.")
    }
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
        try {
          val answerInt: Int = answerNo.toInt
          if (answerInt < 0 || answerInt > 3) {
            return BadRequest(s"Answer number must have value between 0-3")
          }
          val questionInt: Int = questionNo.toInt
          if (questionInt < 0 || questionInt > quiz.rounds.size - 1) {
            return BadRequest(s"Question number must have value between 0-${quiz.rounds.size - 1}")
          }
          val correctDefinitions: List[String] = quiz.rounds(questionInt).word.description
          val selectedDefinition: String = quiz.rounds(questionInt).options(answerInt)
          quiz.rounds(questionInt).answer = Option(answerInt)
          val isCorrect = correctDefinitions.contains(selectedDefinition)
          quiz.rounds(questionInt).correct = Option(isCorrect)
          Ok(isCorrect.toString)
        } catch {
          case _: NumberFormatException => BadRequest("Question and answer number must be of integer type.")
        }
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
   * @param mode unique identifier containing the concrete quiz mode for the generated round
   * @param language string containing the language selection for the generated round
   * @param forbiddenWords list of currently used quiz words which cannot overlap while generating this round
   * @throws QuizServiceException when the round object cannot be generated
   * @return generated round object with random word and 4 answer options
   */
  @throws(classOf[QuizServiceException])
  private def generateRound(mode: Int, language: String, forbiddenWords: List[String] = List.empty): Round = {
    try {
      var word: Word = null
      do {
        word = wordService.getRandomWord(mode, language)
      } while (forbiddenWords.contains(word.name))
      Round(word, generateOptions(word.description, mode, language, word.category), None, None)
    } catch {
      case e: WordServiceException => throw new QuizServiceException(e.getMessage)
    }
  }

  /**
   * Method used to generate specified number of rounds
   * @param size desired number of rounds to be generated
   * @param mode desired quiz mode (as an unique identifier) to be generated
   * @param language string containing the language selection used in the generated rounds
   * @return list of specified number of rounds
   */
  private def generateRounds(size: Int, mode: Int, language: String): List[Round] = {
    var rounds: List[Round] = List.fill(size)(generateRound(mode, language)).distinctBy(_.word.name)
    for (_ <- 0 until size-rounds.length) {
      val replacement: Round = generateRound(mode, language, rounds.map(r => r.word.name))
      rounds = rounds.appended(replacement)
    }
    rounds
  }

  /**
   * Method used to generate 4 answer options
   * @param correctDefinitions correct word definitions (from which one will be added to answer options)
   * @param language of the options to draw the remaining 3 answer options
   * @param category word category from which to draw the remaining 3 answer options
   * @return list of possible 4 answer options
   */
  private def generateOptions(correctDefinitions: List[String],
                              mode: Int, language: String, category: String): List[String] = {
    val incorrectDefinitions: List[String] = wordService.getWordsByCategory(mode, language, category)
      .map(w => Random.shuffle(w.description).head)
      .filter(!correctDefinitions.contains(_))
      .distinct
    val incorrectOptions: List[String] = Random.shuffle(incorrectDefinitions).take(3)
    val correctOption: String = correctDefinitions.apply(Random.nextInt(correctDefinitions.length))
    val options: List[String] = (incorrectOptions :+ correctOption).distinct
    Random.shuffle(options)
  }

  /**
   * Method used to generate a new quiz object
   * @param size desired size of quiz (number of questions)
   * @param mode identifier containing the quiz mode to generate
   * @param language string containing the language selection used in the new quiz object
   * @return generated quiz object
   */
  private def generateQuiz(size: Int, mode: Int, language: String): Quiz = {
    Quiz(generateRounds(size, mode, language), 0)
  }
}
