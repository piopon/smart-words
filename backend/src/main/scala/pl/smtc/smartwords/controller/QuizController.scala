package pl.smtc.smartwords.controller

import cats.effect._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.service._

class QuizController(quizDB: QuizDatabase, wordDB: WordDatabase) {

  object OptionalQuizStartParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("size")

  /**
   * Routes (request -> response) for quiz endpoints/resources
   * <ul>
   *  <li>Start a new quiz: <u>POST</u> /quiz/start?size=10 -> RET: OK 200 + {id} / ERR 500</li>
   *  <li>Receive specific question: <u>GET</u> /quiz/{id}/question/{no} -> RET: OK 200 + Round JSON / ERR 404</li>
   *  <li>Send question answer: <u>POST</u> /quiz/{id}/question/{no}/{answerNo} -> RET: OK 200 / ERR 404</li>
   *  <li>End quiz and get result: <u>GET</u> /quiz/{id}/stop -> RET: OK 200 / ERR 404</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: QuizService = new QuizService(quizDB, wordDB)
    val dsl = Http4sDsl[IO]
    import dsl._
    HttpRoutes.of[IO] {
      case POST -> Root / "start" :? OptionalQuizStartParamMatcher(maybeSize) =>
        service.startQuiz(maybeSize)
      case GET -> Root / UUIDVar(quizId) / "question" / questionNo =>
        service.getQuizQuestionNo(quizId, questionNo)
      case POST -> Root / UUIDVar(quizId) / "question" / questionNo / answerNo =>
        service.postQuizQuestionNo(quizId, questionNo, answerNo)
      case GET -> Root / UUIDVar(quizId) / "stop" =>
        service.stopQuiz(quizId)
    }
  }
}
