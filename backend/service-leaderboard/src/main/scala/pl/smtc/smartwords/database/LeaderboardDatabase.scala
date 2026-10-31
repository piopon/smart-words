package pl.smtc.smartwords.database

import pl.smtc.smartwords.model._

import java.nio.file.{Files, Paths}
import scala.collection.mutable.ListBuffer

class LeaderboardDatabase(dataDirectory: Option[Path] = sys.env.get("LEADERBOARD_DATA_DIR").map(Paths.get(_)),
                          leaderboardFileName: String = sys.env.getOrElse("LEADERBOARD_DATA_FILE", "leaderboard.json")) {

  private val entries = ListBuffer[LeaderboardEntry]()
  private val databaseDir: Path = dataDirectory.getOrElse(Paths.get("."))
  private val databaseFile: Path = databaseDir.resolve(leaderboardFileName)

  initializeDataDirectory()

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

  /**
   * Create custom data directory if configured.
   */
  private def initializeDataDirectory(): Unit = {
    Files.createDirectories(databaseDir)
  }
}
