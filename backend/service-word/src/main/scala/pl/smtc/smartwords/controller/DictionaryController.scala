package pl.smtc.smartwords.controller

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.service._

class DictionaryController(wordDB: WordDatabase) {

  /**
   * Routes (request -> response) for dictionaries endpoints/resources
   * <ul>
   *  <li>Receive all used dictionary data: <u>GET</u> /dictionaries -> OK 200+JSON | ERR 500</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: DictionaryService = new DictionaryService(wordDB)
    val dsl = Http4sDsl[IO]; import dsl._
    HttpRoutes.of[IO] {
      case GET -> Root => service.getDictionaries
    }
  }
}
