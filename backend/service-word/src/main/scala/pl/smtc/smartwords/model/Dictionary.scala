package pl.smtc.smartwords.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Model class representing a word dictionary object with source file, game type, mode and language
 * @param file word source file name (not full path, just the filename itself)
 * @param game type of game for which this word should be used
 * @param mode identifier related to the game for which this word should be used
 * @param language language of the word
 */
case class Dictionary(var file: String, game: String, mode: Option[Int], language: String)

object Dictionary {
  /**
   * Method used to create an empty dictionary object (no file source, game and language)
   * @return empty dictionary object
   */
  def empty(): Dictionary = Dictionary("", "", "", "")

  /**
   * Method used to create a dictionary object based on the file name (the file name itself is stored in this object)
   * @param file source file name from which the dictionary object will be generated
   * @return dictionary object with data retrieved from input file name
   */
  def fromFile(file: String): Dictionary = {
    var usedGameType = "quiz"
    var usedGameMode = "0"
    var usedLanguage = "pl"
    val parts: Array[String] = file.substring(0, file.indexOf("@")).split("-")
    if (parts.length == 4 && parts.apply(0).equals("words")) {
      usedGameType = parts.apply(1)
      usedGameMode = parts.apply(2)
      usedLanguage = parts.apply(3)
    }
    Dictionary(file, usedGameType, usedGameMode, usedLanguage)
  }

  /**
   * Method used to generate new dictionary object with file name based on language
   * @param mode identifier of the word used to generate new dictionary file
   * @param language of the word used to generate new dictionary file
   * @return dictionary object with generated values
   */
  def generate(mode: String, language: String): Dictionary = {
    Dictionary(generateDictionaryFileName(mode, language), "quiz", mode, language)
  }

  /**
   * Method used to generate new dictionary file name
   * @param mode identifier which should be used while generating file name
   * @param language of the dictionary file name
   * @return generated dictionary file name containing current date with JSON extension
   */
  private def generateDictionaryFileName(mode: String, language: String): String = {
    val generatedDescription: String = DateTimeFormatter.ofPattern("YYYY-MM-dd").format(LocalDate.now())
    "words-quiz-" + mode + "-" + language + "@" + generatedDescription + ".json"
  }
}