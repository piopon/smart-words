package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object QuizDao {

  /**
   * Method used to receive round encoder (to JSON)
   * @return round object encoder
   */
  def getRoundEncoder: Encoder[Round] = Encoder.instance {
    (round: Round) => json"""{"word": ${round.word.name},
                              "options": ${round.options},
                              "answer": ${round.answer},
                              "correct": ${round.correct}}"""
  }

  /**
   * Method used to receive quiz summary encoder (to JSON)
   * @return quiz summary object encoder
   */
  def getSummaryEncoder: Encoder[QuizSummary] = Encoder.instance {
    (summary: QuizSummary) => json"""{"scorePercent": ${summary.scorePercent},
                                      "correctAnswers": ${summary.correctAnswers},
                                      "totalQuestions": ${summary.totalQuestions},
                                      "mode": ${summary.mode}}"""
  }
}
