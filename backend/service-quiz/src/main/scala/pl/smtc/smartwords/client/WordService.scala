package pl.smtc.smartwords.client

import cats.effect._
import cats.effect.unsafe.implicits._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.ember.client._
import org.http4s.implicits._
import pl.smtc.smartwords.model._

import scala.concurrent.duration.DurationDouble

class WordService {

  val address: Uri = uri"http://localhost:1111"
  val wordsEndpoint: Uri = address.withPath(path"words")
  val healthEndpoint: Uri = address.withPath(path"health")

  implicit val WordsDecoder: EntityDecoder[IO, List[Word]] = jsonOf[IO, List[Word]]

  /**
   * Method used to check if word service is alive and working correctly
   * @return true if word service is correctly running, false otherwise
   */
  def isAlive: Boolean = {
    try {
      setGetHealthRequest(healthEndpoint) match {
        case None => false
        case Some(status) => status.endsWith("OK")
      }
    } catch {
      case _: Exception => false
    }
  }

  /**
   * Method used to communicate with word service and retrieve a random word
   * @param language type of language for the word to be retrieved
   * @throws WordServiceException when the response from word service is invalid
   * @return random word object
   */
  @throws(classOf[WordServiceException])
  def getRandomWord(language: String): Word = {
    val endpoint: Uri = wordsEndpoint.addSegment(language)
                                     .withQueryParam("size", "1")
                                     .withQueryParam("random", "true")
    val receivedWord: List[Word] = sendGetWordsRequest(endpoint)
    if (receivedWord.isEmpty) {
      throw new WordServiceException("Invalid input parameter(s).")
    }
    receivedWord.head
  }

  /**
   * Method used to communicate with word service and retrieve all words with specified category
   * @param category type of words category to be retrieved
   * @return list of all words with specified category
   */
  def getWordsByCategory(language: String, category: String): List[Word] = {
    sendGetWordsRequest(wordsEndpoint.addSegment(language)
                                     .withQueryParam("cat", category))
  }

  /**
   * Method used to send GET words request to word service
   * @param endpoint to be send as a request to word service
   * @return list of received words
   */
  private def sendGetWordsRequest(endpoint: Uri): List[Word] = {
    EmberClientBuilder.default[IO].build.use(client => client.expect[List[Word]](GET(endpoint))).unsafeRunSync()
  }

  /**
   * Method used to send GET health request to word service
   * @param endpoint to be send as a request to word service
   * @return health status as a String
   */
  private def setGetHealthRequest(endpoint: Uri): Option[String] = {
    EmberClientBuilder.default[IO].build.use(client => client.expect[String](GET(endpoint))).unsafeRunTimed(1.0.seconds)
  }
}
