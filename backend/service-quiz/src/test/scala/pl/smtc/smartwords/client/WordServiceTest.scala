package pl.smtc.smartwords.client

import pl.smtc.smartwords.model._

import scala.util.Random

/**
 * <b>IMPORTANT:</b> This class is a test implementation of a word client service, not a class with unit tests
 */
class WordServiceTest(alive: Boolean = true, wordFail: Boolean = false, categoryFail: Boolean = false) extends IWordService {

  override def isAlive: Boolean = alive

  override def getRandomWord(mode: Int, language: String): Word = {
    if (wordFail) {
      throw new WordServiceException("Invalid input parameter(s) - getRandomWord error!")
    }
    val randomIdString: String = Random.nextInt().toString
    val randomWordName: String = "word-" + language + "-" + mode.toString + randomIdString
    Word(randomWordName, "verb", List("definition-main-" + randomIdString, "alternate-definition-" + randomIdString))
  }

  override def getWordsByCategory(mode: Int, language: String, category: String): List[Word] = {
    if (categoryFail) {
      throw new WordServiceException("Invalid input parameter(s) - getWordsByCategory error!")
    }
    List(
      Word("word-" + language + "-" + mode.toString + "-00", category, List("def-0", "alt-00")),
      Word("word-" + language + "-" + mode.toString + "-01", category, List("def-1", "alt-11")),
      Word("word-" + language + "-" + mode.toString + "-02", category, List("def-2", "alt-22")),
      Word("word-" + language + "-" + mode.toString + "-03", category, List("def-3", "alt-33")),
      Word("word-" + language + "-" + mode.toString + "-04", category, List("def-4", "alt-44")),
      Word("word-" + language + "-" + mode.toString + "-05", category, List("def-5", "alt-55")),
      Word("word-" + language + "-" + mode.toString + "-06", category, List("def-6", "alt-66")),
      Word("word-" + language + "-" + mode.toString + "-07", category, List("def-7", "alt-77")),
      Word("word-" + language + "-" + mode.toString + "-08", category, List("def-8", "alt-88")),
      Word("word-" + language + "-" + mode.toString + "-09", category, List("def-9", "alt-99"))
    )
  }
}
