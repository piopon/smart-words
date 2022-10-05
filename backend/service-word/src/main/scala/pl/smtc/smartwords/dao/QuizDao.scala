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
}
