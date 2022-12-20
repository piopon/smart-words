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

class QuizService(quizDB: QuizDatabase) {

  implicit val WordService: WordService = new WordService()
  implicit val RoundEncoder: Encoder[Round] = QuizDao.getRoundEncoder

  private final val defaultQuizSize: Int = 10
  private final val defaultQuizLang: String = "pl"

  /**
   * Method used to start a new quiz
   * @param maybeSize an optional size value (if none then default value will be applied)
   * @param maybeLanguage an optional language value (if non then the default value will be applied)
   * @return response with appropriate status
   */
  def startQuiz(maybeSize: Option[Int], maybeMode: Option[Int], maybeLanguage: Option[String]): IO[Response[IO]] = {
    val size: Int = maybeSize match {
      case None => defaultQuizSize
      case Some(size) => size
    }
    val language: String = maybeLanguage match {
      case None => defaultQuizLang
      case Some(language) => language
    }
    if (WordService.isAlive) {
      try {
        Ok(quizDB.addQuiz(generateQuiz(size, language)).toString)
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
        val correctDefinitions: List[String] = quiz.rounds(questionNo.toInt).word.description
        val selectedDefinition: String = quiz.rounds(questionNo.toInt).options(answerNo.toInt)
        quiz.rounds(questionNo.toInt).answer = Option(answerNo.toInt)
        val isCorrect = correctDefinitions.contains(selectedDefinition)
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
   * @param language string containing the language selection for the generated round
   * @param forbiddenWords list of currently used quiz words which cannot overlap while generating this round
   * @throws QuizServiceException when the round object cannot be generated
   * @return generated round object with random word and 4 answer options
   */
  @throws(classOf[QuizServiceException])
  private def generateRound(language: String, forbiddenWords: List[String] = List.empty): Round = {
    try {
      var word: Word = null
      do {
        word = WordService.getRandomWord(language)
      } while (forbiddenWords.contains(word.name))
      Round(word, generateOptions(word.description, language, word.category), None, None)
    } catch {
      case e: WordServiceException => throw new QuizServiceException(e.getMessage)
    }
  }

  /**
   * Method used to generate specified number of rounds
   * @param size desired number of rounds to be generated
   * @param language string containing the language selection used in the generated rounds
   * @return list of specified number of rounds
   */
  private def generateRounds(size: Int, language: String): List[Round] = {
    var rounds: List[Round] = List.fill(size)(generateRound(language)).distinctBy(_.word.name)
    for (_ <- 0 until size-rounds.length) {
      val replacement: Round = generateRound(language, rounds.map(r => r.word.name))
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
  private def generateOptions(correctDefinitions: List[String], language: String, category: String): List[String] = {
    val incorrectDefinitions: List[String] = WordService.getWordsByCategory(language, category)
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
   * @param language string containing the language selection used in the new quiz object
   * @return generated quiz object
   */
  private def generateQuiz(size: Int, language: String): Quiz = {
    Quiz(generateRounds(size, language), 0)
  }
}
