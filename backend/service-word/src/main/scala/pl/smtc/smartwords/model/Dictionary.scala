package pl.smtc.smartwords.model

case class Dictionary(var file: String, game: String, language: String)

object Dictionary {
  def empty(): Dictionary = Dictionary("", "", "")

  def fromFile(file: String): Dictionary = {
    Dictionary(file, "", "")
  }
}