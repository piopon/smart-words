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
import pl.smtc.smartwords.dao.WordDao

class WordController(wordDB: WordDatabase) {

  implicit val WordDecoder: Decoder[Word] = WordDao.getWordDecoder

  implicit val categoryParamDecoder: QueryParamDecoder[Category.Value] =
    QueryParamDecoder[String].map(categoryStr => Category.fromString(categoryStr))
  object OptionalCategoryParamMatcher extends OptionalQueryParamDecoderMatcher[Category.Value]("cat")

  /**
   * Routes (request -> response) for admin endpoints/resources
   * <ul>
   *  <li>Receive all words: <u>GET</u> /admin/words -> RET: OK 200 + ALL WORDS JSON / ERR 500</li>
   *  <li>Receive category-specific words: <u>GET</u> /admin/words?cat=adj -> RET: OK 200 + Word JSON / ERR 500</li>
   *  <li>Add a new word: <u>POST</u> /admin/words + Word JSON -> RET: OK 200 / ERR 500</li>
   *  <li>Delete word: <u>DELETE</u> /admin/words/{name} -> RET: OK 200 / ERR 404</li>
   *  <li>Update word: <u>PUT</u> /admin/words/{name} + Word JSON -> RET: OK 200 + Word JSON / ERR 404</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: WordService = new WordService(wordDB)
    val dsl = Http4sDsl[IO]; import dsl._
    implicit val wordDecoder: EntityDecoder[IO, Word] = jsonOf[IO, Word]
    HttpRoutes.of[IO] {
      case GET -> Root :? OptionalCategoryParamMatcher(maybeCategory) =>
        service.getWords(maybeCategory)
      case request@POST -> Root  =>
        for {
          newWord <- request.as[Word]
          response <- service.addWord(newWord)
        } yield response
      case request@PUT -> Root / name =>
        for {
          newWord <- request.as[Word]
          response <- service.updateWord(name, newWord)
        } yield response
      case DELETE -> Root / name =>
        service.deleteWord(name)
    }
  }
}
