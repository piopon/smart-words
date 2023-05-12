package pl.smtc.smartwords

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.middleware._
import org.http4s.ember.server._
import pl.smtc.smartwords.client.WordService
import pl.smtc.smartwords.controller._
import pl.smtc.smartwords.database._

import scala.concurrent.duration.DurationInt

object ServiceQuizApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    // initialize databases
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val modeDatabase: ModeDatabase = new ModeDatabase()
    if (!modeDatabase.loadDatabase()) {
      return IO.canceled.as(ExitCode.Error)
    }
    // initialize other services clients
    val wordServiceClient: WordService = new WordService()
    // initialize controllers
    val healthController: HealthController = new HealthController()
    val modeController: ModeController = new ModeController(modeDatabase)
    val quizController: QuizController = new QuizController(quizDatabase, wordServiceClient)
    // setup router
    val config = CORSConfig(anyOrigin = true, allowCredentials = true, 1.day.toSeconds, anyMethod = true)
    val apis = Router(
      "/health" -> CORS(healthController.getRoutes, config),
      "/modes" -> CORS(modeController.getRoutes, config),
      "/quiz" -> CORS(quizController.getRoutes, config)
    ).orNotFound
    // start server
    for {
      server <- EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"2222")
        .withHttpApp(apis)
        .withIdleTimeout(30.minutes)
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
