package pl.smtc.smartwords.model

/**
 * Model class representing summary details returned after ending a quiz.
 * @param scorePercent ratio of correct answers to total questions
 * @param correctAnswers number of correctly answered questions
 * @param totalQuestions total number of questions in quiz
 * @param mode quiz mode identifier
 */
case class QuizSummary(scorePercent: Float, correctAnswers: Int, totalQuestions: Int, mode: Int)