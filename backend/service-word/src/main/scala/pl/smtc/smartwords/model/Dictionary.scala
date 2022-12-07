package pl.smtc.smartwords.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Dictionary(var file: String, game: String, language: String)

object Dictionary {
  def empty(): Dictionary = Dictionary("", "", "")

  def fromFile(file: String): Dictionary = {
    Dictionary(file, "", "")
  }

  def generate(language: String): Dictionary = {
    Dictionary(generateDictionaryFileName(), "", "")
  }

  /**
   * Method used to generate new dictionary file name
   * @return generated dictionary file name containing current date with JSON extension
   */
  private def generateDictionaryFileName(): String = {
    DateTimeFormatter.ofPattern("YYYY-MM-dd").format(LocalDate.now()) + ".json"
  }
}