package pl.smtc.smartwords.model

/**
 * Model class representing a complete quiz containing several rounds (smart words questions)
 * @param rounds a collection of rounds/questions
 * @param score current correct answers counter
 * @param mode current quiz mode identifier
 */
case class Quiz(rounds: List[Round], score: Int, mode: Int)
