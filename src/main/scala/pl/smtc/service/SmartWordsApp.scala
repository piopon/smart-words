package pl.smtc.service

object SmartWordsApp extends App {

  object Category extends Enumeration {
    type Category = Value
    val verb, adverb, noun, adjective = Value
  }
  case class Word(name: String, category: Category.Value, definition: String)
  case class Round(word: Word, options: List[String], answer: String)
  case class Quiz(rounds: Map[Round, Boolean], score: Int)

  /*
   * API ENDPOINTS + APPLICATION FLOW
   * - POST: start new quiz (how many questions/answers immediately) -> OK / error (to little words)
   * - GET: questionNo (1 ... 10) -> OK + JSON with word and 4 options
   * - POST: questionNo/answerNo -> OK + JSON correct/incorrect answer
   * - GET: result -> OK + JSON with score %
   * - GET: summary -> OK + list of all questions with answers and correct definitions
   *
   * ADMIN COMMANDS
   * - GET: all words
   * - GET: all words of selected category
   * - POST: add a new word
   * - DELETE: remove a word
   * - PUT: modify a word
   */
}
