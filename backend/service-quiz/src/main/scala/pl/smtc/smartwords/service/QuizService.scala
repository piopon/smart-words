package pl.smtc.smartwords.service

import cats.effect._
import cats.effect.unsafe.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.ember.client._
import org.http4s.implicits._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.dao._

import java.util.UUID
import scala.util.Random

class QuizService(quizDB: QuizDatabase) {

  implicit val RoundEncoder: Encoder[Round] = QuizDao.getRoundEncoder
  implicit val WordsDecoder: EntityDecoder[IO, List[Word]] = jsonOf[IO, List[Word]]

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
   * @param forbiddenWords list of currently used quiz words which cannot overlap while generating this round
   * @return generated round object with random word and 4 answer options
   */
  private def generateRound(forbiddenWords: List[String] = List.empty): Round = {
    var word: Word = null
    do {
      word = getRandomWord
    } while (forbiddenWords.contains(word.name))
    Round(word, generateOptions(word.description, word.category), None, None)
  }

  /**
   * Method used to generate specified number of rounds
   * @param size desired number of rounds to be generated
   * @return list of specified number of rounds
   */
  private def generateRounds(size: Int): List[Round] = {
    var rounds: List[Round] = List.fill(size)(generateRound()).distinctBy(_.word.name)
    for (_ <- 0 until size-rounds.length) {
      val replacement: Round = generateRound(rounds.map(r => r.word.name))
      rounds = rounds.appended(replacement)
    }
    rounds
  }

  /**
   * Method used to generate 4 answer options
   * @param correctDefinitions correct word definitions (from which one will be added to answer options)
   * @param category word category from which to draw the remaining 3 answer options
   * @return list of possible 4 answer options
   */
  private def generateOptions(correctDefinitions: List[String], category: String): List[String] = {
    val incorrectDefinitions: List[String] = getWordsByCategory(category)
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
   * @return generated quiz object
   */
  private def generateQuiz(size: Int): Quiz = {
    Quiz(generateRounds(size), 0)
  }

  /**
   * Method used to communicate with words service and retrieve a random word
   * @return random word object
   */
  private def getRandomWord: Word = {
    var word: Word = null
    val getWordRequest = GET(uri"http://localhost:1111/words?size=1&random=true")
    EmberClientBuilder.default[IO].build.use(client =>
      client.expect[List[Word]](getWordRequest).map(response => word = response.head)
    ).unsafeRunSync()
    word
  }

  /**
   * Method used to communicate with words service and retrieve all words with specified category
   * @param category category type of words to be retrieved
   * @return list of all words with specified category
   */
  private def getWordsByCategory(category: String): List[Word] = {
    var categoryWords: List[Word] = List()
    val wordServiceRequest = GET(uri"http://localhost:1111/words".withQueryParam("cat", category))
    EmberClientBuilder.default[IO].build.use(client =>
      client.expect[List[Word]](wordServiceRequest).map(response => categoryWords = response)
    ).unsafeRunSync()
    categoryWords
  }
}
