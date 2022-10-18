package pl.smtc.smartwords.database

import pl.smtc.smartwords.model._

import java.util.UUID
import scala.collection.mutable

class QuizDatabase {

  private val activeQuizzes: mutable.Map[UUID, Quiz] = mutable.Map()

  /**
   * Method used to receive a single quiz object from memory with specified ID
   * @param uuid an ID of a quiz to be received
   * @return non empty if quiz was present, None otherwise
   */
  def getQuiz(uuid: UUID): Option[Quiz] = activeQuizzes.get(uuid)

  /**
   * Method used to add new quiz to memory
   * @param quiz new quiz to be added to memory
   * @return  UUID of the newly added quiz object
   */
  def addQuiz(quiz: Quiz): UUID = {
    val newUuid: UUID = UUID.randomUUID()
    activeQuizzes.put(newUuid, quiz)
    newUuid
  }

  /**
   * Method used to remove quiz from memory
   * @param uuid UUID of the quiz to be removed from memory
   * @return non empty with removed quiz object, None otherwise (quiz does not exist in memory)
   */
  def removeQuiz(uuid: UUID): Option[Quiz] = activeQuizzes.remove(uuid)
}
