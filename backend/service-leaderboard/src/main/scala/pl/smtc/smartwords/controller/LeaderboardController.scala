package pl.smtc.smartwords.controller

import cats.effect._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.service._

class LeaderboardController(database: LeaderboardDatabase) {

  private object OptionalLimitParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")

  /**
   * Routes (request -> response) for leaderboard endpoints/resources.
    *
    * @return all leaderboard HTTP routes
   */
  def getRoutes: HttpRoutes[IO] = {
    val service = new LeaderboardService(database)
    val dsl = Http4sDsl[IO]; import dsl._
    implicit val submitDecoder: EntityDecoder[IO, LeaderboardSubmit] = jsonOf[IO, LeaderboardSubmit]

    HttpRoutes.of[IO] {
      case request @ POST -> Root / "entries" =>
        for {
          payload <- request.as[LeaderboardSubmit]
          response <- service.submitEntry(payload)
        } yield response

      case GET -> Root / "entries" / game / mode :? OptionalLimitParamMatcher(maybeLimit) =>
        service.getTopEntries(game, mode, maybeLimit)
    }
  }
}
