package pl.smtc.smartwords.database

import pl.smtc.smartwords.model._

import java.util.UUID
import scala.collection.mutable

class QuizDatabase {

  private val activeQuizzes: mutable.Map[UUID, Quiz] = mutable.Map()

  def getQuiz(uuid: UUID): Option[Quiz] = activeQuizzes.get(uuid)
}
