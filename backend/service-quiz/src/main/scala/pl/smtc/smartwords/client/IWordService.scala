package pl.smtc.smartwords.client

import pl.smtc.smartwords.model._

trait IWordService {
  def isAlive: Boolean
  def getRandomWord(mode: Int, language: String): Word
  def getWordsByCategory(mode: Int, language: String, category: String): List[Word]
}
