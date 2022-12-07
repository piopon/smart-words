package pl.smtc.smartwords.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Dictionary(var file: String, game: String, language: String)

object Dictionary {
  def empty(): Dictionary = Dictionary("", "", "")

  def fromFile(file: String): Dictionary = {
    Dictionary(file, "quiz", "")
  }

  def generate(language: String): Dictionary = {
    Dictionary(generateDictionaryFileName(language), "quiz", language)
  }

  /**
   * Method used to generate new dictionary file name
   * @return generated dictionary file name containing current date with JSON extension
   */
  private def generateDictionaryFileName(language: String): String = {
    "words-quiz-" + language + "-" + DateTimeFormatter.ofPattern("YYYY-MM-dd").format(LocalDate.now()) + ".json"
  }
}