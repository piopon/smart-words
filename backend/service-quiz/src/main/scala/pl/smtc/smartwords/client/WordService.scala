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
    setGetHealthRequest(healthEndpoint).endsWith("OK")
  }

  /**
   * Method used to communicate with word service and retrieve a random word
   * @return random word object
   */
  def getRandomWord: Word = {
    sendGetWordsRequest(wordsEndpoint.withQueryParam("size", "1")
                                     .withQueryParam("random", "true")).head
  }

  /**
   * Method used to communicate with word service and retrieve all words with specified category
   * @param category type of words category to be retrieved
   * @return list of all words with specified category
   */
  def getWordsByCategory(category: String): List[Word] = {
    sendGetWordsRequest(wordsEndpoint.withQueryParam("cat", category))
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
  private def setGetHealthRequest(endpoint: Uri): String = {
    EmberClientBuilder.default[IO].build.use(client => client.expect[String](GET(endpoint))).unsafeRunSync()
  }
}
