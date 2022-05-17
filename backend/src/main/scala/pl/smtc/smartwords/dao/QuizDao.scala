package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object QuizDao {
  def getRoundEncoder: Encoder[Round] = Encoder.instance {
    (round: Round) => json"""{"word": ${round.word.name}, "options": ${round.options}}"""
  }
}
