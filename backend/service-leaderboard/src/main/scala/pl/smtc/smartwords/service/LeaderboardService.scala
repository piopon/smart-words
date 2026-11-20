package pl.smtc.smartwords.service

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class LeaderboardService(database: LeaderboardDatabase) {

  private val defaultLimit = 10
  private val maxLimit = 100

  /**
   * Submit new leaderboard attempt.
    *
    * @param submit leaderboard submit payload
    * @return created entry response or validation error response
   */
  def submitEntry(submit: LeaderboardSubmit): IO[Response[IO]] = {
    val game = submit.game.trim.toLowerCase
    val username = submit.username.trim

    if (game.isEmpty) {
      return BadRequest("Field 'game' must not be empty.")
    }
    if (username.isEmpty || username.length > 24) {
      return BadRequest("Field 'username' must contain 1-24 characters.")
    }
    if (submit.mode < 0) {
      return BadRequest("Field 'mode' must be greater than or equal to 0.")
    }
    if (submit.totalQuestions <= 0) {
      return BadRequest("Field 'totalQuestions' must be greater than 0.")
    }
    if (submit.correctAnswers < 0 || submit.correctAnswers > submit.totalQuestions) {
      return BadRequest("Field 'correctAnswers' must be between 0 and totalQuestions.")
    }
    if (submit.scorePercent < 0.0f || submit.scorePercent > 1.0f) {
      return BadRequest("Field 'scorePercent' must be between 0.0 and 1.0.")
    }

    val entry = LeaderboardEntry(
      game = game,
      mode = submit.mode,
      username = username,
      scorePercent = submit.scorePercent,
      totalQuestions = submit.totalQuestions,
      correctAnswers = submit.correctAnswers,
      timestamp = System.currentTimeMillis()
    )
    Created(database.addEntry(entry).asJson)
  }

  /**
   * Get top leaderboard entries for selected game and mode.
    *
    * @param game game identifier from request path
    * @param mode mode identifier from request path
    * @param maybeLimit optional query limit value
    * @return top entries response or validation error response
   */
  def getTopEntries(game: String, mode: String, maybeLimit: Option[Int]): IO[Response[IO]] = {
    val normalizedGame = game.trim.toLowerCase
    if (normalizedGame.isEmpty) {
      return BadRequest("Path parameter 'game' must not be empty.")
    }

    val resolvedMode: Int = try {
      mode.toInt
    } catch {
      case _: NumberFormatException => return BadRequest("Path parameter 'mode' must be an integer.")
    }

    if (resolvedMode < 0) {
      return BadRequest("Path parameter 'mode' must be greater than or equal to 0.")
    }

    val resolvedLimit = maybeLimit.getOrElse(defaultLimit)
    if (resolvedLimit <= 0 || resolvedLimit > maxLimit) {
      return BadRequest(s"Query parameter 'limit' must be between 1 and $maxLimit.")
    }

    Ok(database.getTopEntries(normalizedGame, resolvedMode, resolvedLimit).asJson)
  }
}
