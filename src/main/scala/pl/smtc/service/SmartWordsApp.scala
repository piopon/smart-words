package pl.smtc.service

object SmartWordsApp extends App {

  object Category extends Enumeration {
    type Category = Value
    val verb, adverb, noun, adjective = Value
  }
  case class Word(name: String, category: Category.Value, definition: String)
  case class Round(word: Word, options: List[String], answer: String)
  case class Quiz(rounds: Map[Round, Boolean], score: Int)

}
