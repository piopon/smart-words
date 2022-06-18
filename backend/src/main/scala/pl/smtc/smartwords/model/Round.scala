package pl.smtc.smartwords.model

/**
 * Model class representing a single round of a quiz
 * @param word selected word which a user has to figure out
 * @param options the list of possible answers (will be matched from word category)
 * @param correct true if got correct answer, false otherwise. If no answer yet then None
 * @param answer value representing answered option no. If no answer yet then None
 */
case class Round(word: Word, options: List[String], var correct: Option[Boolean], var answer: Option[Int])
