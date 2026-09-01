package pl.smtc.smartwords

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.middleware._
import org.http4s.ember.server._
import pl.smtc.smartwords.client._
import pl.smtc.smartwords.config._
import pl.smtc.smartwords.controller._
import pl.smtc.smartwords.database._

import scala.concurrent.duration.DurationInt

object ServiceQuizApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val appConfig: QuizAppConfig = QuizAppConfig.load()
    val serverHost: Host = Host.fromString(appConfig.service.host).getOrElse(ipv4"0.0.0.0")
    val serverPort: Port = Port.fromInt(appConfig.service.port).getOrElse(port"2222")
    // initialize databases
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val modeDatabase: ModeDatabase = new ModeDatabase(appConfig.data.modeFile)
    if (!modeDatabase.loadDatabase()) {
      return IO.canceled.as(ExitCode.Error)
    }
    // initialize other services clients
    val wordServiceClient: WordService = new WordService(Some(appConfig.wordService))
    // initialize controllers
    val healthController: HealthController = new HealthController()
    val modeController: ModeController = new ModeController(modeDatabase)
    val quizController: QuizController = new QuizController(quizDatabase, wordServiceClient)
    // setup router
    val config = CORSConfig(
      anyOrigin = appConfig.cors.anyOrigin,
      allowCredentials = appConfig.cors.allowCredentials,
      maxAge = appConfig.cors.maxAgeSeconds,
      anyMethod = appConfig.cors.anyMethod
    )
    val apis = Router(
      "/health" -> CORS(healthController.getRoutes, config),
      "/modes" -> CORS(modeController.getRoutes, config),
      "/quiz" -> CORS(quizController.getRoutes, config)
    ).orNotFound
    // start server
    for {
      server <- EmberServerBuilder.default[IO]
        .withHost(serverHost)
        .withPort(serverPort)
        .withHttpApp(apis)
        .withIdleTimeout(appConfig.service.idleTimeoutMinutes.minutes)
        .withErrorHandler { case err => IO(err.printStackTrace()).as(Response(status = Status.InternalServerError)) }
        .build
    } yield server
  }.use(server => {
    val serverAddress = server.address.getAddress.getHostAddress
    val serverPort = server.address.getPort
    IO.delay(println(s"Service: ${appConfig.service.name}\n" +
      s"- state: started\n" +
      s"- address: IPv6=$serverAddress, port=$serverPort")) >> IO.never.as(ExitCode.Success)
  })
}
