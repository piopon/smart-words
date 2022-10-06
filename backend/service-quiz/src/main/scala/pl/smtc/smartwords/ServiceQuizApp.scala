package pl.smtc.smartwords

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.middleware._
import org.http4s.ember.server._
import pl.smtc.smartwords.controller._
import pl.smtc.smartwords.database._

import scala.concurrent.duration.DurationInt

object ServiceQuizApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val quizController: QuizController = new QuizController()

    val config = CORSConfig(anyOrigin = true, allowCredentials = true, 1.day.toSeconds, anyMethod = true)
    val apis = Router("/quiz" -> CORS(quizController.getRoutes, config)).orNotFound
    for {
      server <- EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"2222")
        .withHttpApp(apis)
        .withErrorHandler { case err => IO(err.printStackTrace()).as(Response(status = Status.InternalServerError)) }
        .build
    } yield server
  }.use(server => {
    val serverAddress = server.address.getAddress.getHostAddress
    val serverPort = server.address.getPort
    IO.delay(println(s"Service: QUIZ\n" +
      s"- state: started\n" +
      s"- address: IPv6=$serverAddress, port=$serverPort")) >> IO.never.as(ExitCode.Success)
  })
}
