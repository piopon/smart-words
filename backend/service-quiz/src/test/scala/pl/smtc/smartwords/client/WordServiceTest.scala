package pl.smtc.smartwords.client

import pl.smtc.smartwords.model._

/**
 * <b>IMPORTANT:</b> This class is a test implementation of a word client service, not a class with unit tests
 */
class WordServiceTest extends IWordService {

  override def isAlive: Boolean = true

  override def getRandomWord(mode: Int, language: String): Word = {
    Word("word-" + language + "-" + mode.toString, "verb", List("definition-main", "alternate-definition"))
  }

  override def getWordsByCategory(mode: Int, language: String, category: String): List[Word] = {
    List(
      Word("word-" + language + "-" + mode.toString + "-1", category, List("def-1", "alt-11")),
      Word("word-" + language + "-" + mode.toString + "-2", category, List("def-2", "alt-22")),
      Word("word-" + language + "-" + mode.toString + "-3", category, List("def-3", "alt-33"))
    )
  }
}
