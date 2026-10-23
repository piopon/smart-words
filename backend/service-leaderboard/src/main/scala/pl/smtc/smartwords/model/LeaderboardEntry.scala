package pl.smtc.smartwords.model

/**
 * Model class representing a stored leaderboard attempt.
 */
case class LeaderboardEntry(game: String,
                            mode: Int,
                            username: String,
                            scorePercent: Float,
                            totalQuestions: Int,
                            correctAnswers: Int,
                            timestamp: Long)
