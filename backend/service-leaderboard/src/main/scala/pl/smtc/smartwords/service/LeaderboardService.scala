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
}
