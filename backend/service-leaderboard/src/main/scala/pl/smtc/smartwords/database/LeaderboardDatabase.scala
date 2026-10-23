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
}
