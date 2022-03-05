package pl.smtc.service

object SmartWordsApp extends App {

  case class Word(name: String, category: String, definition: String)
  case class Round(word: Word, options: List[String], answer: String)

}
