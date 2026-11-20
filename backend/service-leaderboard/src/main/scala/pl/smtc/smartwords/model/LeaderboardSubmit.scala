package pl.smtc.smartwords.model

/**
 * Model class representing leaderboard submit request payload.
 */
case class LeaderboardSubmit(game: String,
                             mode: Int,
                             username: String,
                             scorePercent: Float,
                             totalQuestions: Int,
                             correctAnswers: Int)
