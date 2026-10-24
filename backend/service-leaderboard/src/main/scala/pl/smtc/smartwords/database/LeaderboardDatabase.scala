package pl.smtc.smartwords.database

import pl.smtc.smartwords.model._

import scala.collection.mutable.ListBuffer

class LeaderboardDatabase {

  private val entries = ListBuffer[LeaderboardEntry]()

  /**
   * Add one leaderboard attempt to full history.
   */
  def addEntry(entry: LeaderboardEntry): LeaderboardEntry = synchronized {
    entries += entry
    entry
  }

  /**
   * Return sorted top entries for selected game and mode.
   */
  def getTopEntries(game: String, mode: Int, limit: Int): List[LeaderboardEntry] = synchronized {
    entries
      .filter(entry => entry.game == game && entry.mode == mode)
      .sortWith((left, right) => {
        if (left.scorePercent != right.scorePercent) {
          left.scorePercent > right.scorePercent
        } else if (left.totalQuestions != right.totalQuestions) {
          left.totalQuestions > right.totalQuestions
        } else {
          left.timestamp < right.timestamp
        }
      })
      .take(limit)
      .toList
  }
}
