package pl.smtc.smartwords.controller

import cats.effect._
import org.http4s.circe._
import io.circe._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.service._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.middleware._

class WordController(wordDB: WordDatabase) {

  implicit val WordDecoder: Decoder[Word] = WordDao.getWordDecoder

  object OptionalRandomizeParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Boolean]("random")
  object OptionalCategoryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("cat")
  object OptionalSizeParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Int]("size")

  /**
   * Routes (request -> response) for words endpoints/resources
   * <ul>
   *  <li>Receive all language-specific words: <u>GET</u> /words/{mode}/{lang} -> OK 200+JSON | ERR 500</li>
   *  <li>Receive specified number of words: <u>GET</u> /words/{mode}/{lang}?size=no -> OK 200+JSON | ERR 500</li>
   *  <li>Receive category-specific words: <u>GET</u> /words/{mode}/{lang}?cat=adj -> OK 200+JSON | ERR 500</li>
   *  <li>Receive words in random order: <u>GET</u> /words/{mode}/{lang}?random=bool -> OK 200+JSON | ERR 500</li>
   *  <li>Add a new word: <u>POST</u> /words/{mode}/{lang} + JSON -> OK 200 | ERR 500</li>
   *  <li>Update word: <u>PUT</u> /words/{mode}/{lang}/{name} + JSON -> OK 200+JSON | ERR 404</li>
   *  <li>Delete word: <u>DELETE</u> /words/{mode}/{lang}/{name} -> OK 200 | ERR 404</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val middleware: WordMiddleware = new WordMiddleware()
    val service: WordService = new WordService(wordDB)
    val dsl = Http4sDsl[IO]; import dsl._
    implicit val wordDecoder: EntityDecoder[IO, Word] = jsonOf[IO, Word]
    HttpRoutes.of[IO] {
      case GET -> Root / mode / language :? OptionalCategoryParamMatcher(maybeCategory)
                                         +& OptionalSizeParamMatcher(maybeSize)
                                         +& OptionalRandomizeParamMatcher(maybeRandom) =>
        try {
          val validatedMode: Option[Int] = middleware.validateParameterMode(mode, wordDB.getAvailableModes)
          val validatedLanguage: String = middleware.validateParameterLanguage(language, wordDB.getAvailableLanguages)
          val validatedRandom: Option[Boolean] = middleware.validateParameterRandom(maybeRandom)
          val validatedSize: Option[Int] = middleware.validateParameterSize(maybeSize)
          val validatedCategory: Option[Category.Value] = middleware.validateParameterCategory(maybeCategory)
          service.getWords(validatedMode, validatedLanguage, validatedCategory, validatedSize, validatedRandom)
        } catch {
          case e: WordMiddlewareException => BadRequest(e.getMessage)
        }
      case request@POST -> Root / mode / language =>
        try {
          val validatedMode: Option[Int] = middleware.validateParameterMode(mode, wordDB.getAvailableModes)
          val validatedLanguage: String = middleware.validateParameterLanguage(language, wordDB.getAvailableLanguages)
          for {
            newWord <- request.as[Word]
            response <- service.addWord(validatedMode, validatedLanguage, newWord)
          } yield response
        } catch {
          case e: WordMiddlewareException => BadRequest(e.getMessage)
        }
      case request@PUT -> Root / mode / language / name =>
        try {
          val validatedMode: Option[Int] = middleware.validateParameterMode(mode, wordDB.getAvailableModes)
          val validatedLanguage: String = middleware.validateParameterLanguage(language, wordDB.getAvailableLanguages)
          for {
            newWord <- request.as[Word]
            response <- service.updateWord(validatedMode, validatedLanguage, name, newWord)
          } yield response
        } catch {
          case e: WordMiddlewareException => BadRequest(e.getMessage)
        }
      case DELETE -> Root / mode / language / name =>
        try {
          val validatedMode: Option[Int] = middleware.validateParameterMode(mode, wordDB.getAvailableModes)
          val validatedLanguage: String = middleware.validateParameterLanguage(language, wordDB.getAvailableLanguages)
          service.deleteWord(validatedMode, validatedLanguage, name)
        } catch {
          case e: WordMiddlewareException => BadRequest(e.getMessage)
        }
    }
  }
}
