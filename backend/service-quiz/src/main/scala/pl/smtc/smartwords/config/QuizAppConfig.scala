package pl.smtc.smartwords.config

import com.typesafe.config.ConfigFactory

final case class QuizServiceConfig(
  name: String,
  host: String,
  port: Int,
  idleTimeoutMinutes: Int
)

final case class QuizCorsConfig(
  anyOrigin: Boolean,
  allowCredentials: Boolean,
  maxAgeSeconds: Long,
  anyMethod: Boolean
)

final case class QuizDataConfig(modeFile: String)

final case class QuizWordServiceConfig(
  host: String,
  port: Int,
  baseUrl: String,
  requestTimeoutSeconds: Double
)

final case class QuizRuntimeConfig(defaultSize: Int)

final case class QuizAppConfig(
  service: QuizServiceConfig,
  cors: QuizCorsConfig,
  data: QuizDataConfig,
  wordService: QuizWordServiceConfig,
  quiz: QuizRuntimeConfig
)

object QuizAppConfig {

  def load(): QuizAppConfig = {
    val config = ConfigFactory.load()

    QuizAppConfig(
      service = QuizServiceConfig(
        name = config.getString("service.name"),
        host = config.getString("service.host"),
        port = config.getInt("service.port"),
        idleTimeoutMinutes = config.getInt("service.idle-timeout-minutes")
      ),
      cors = QuizCorsConfig(
        anyOrigin = config.getBoolean("cors.any-origin"),
        allowCredentials = config.getBoolean("cors.allow-credentials"),
        maxAgeSeconds = config.getLong("cors.max-age-seconds"),
        anyMethod = config.getBoolean("cors.any-method")
      ),
      data = QuizDataConfig(
        modeFile = config.getString("data.mode-file")
      ),
      wordService = QuizWordServiceConfig(
        host = config.getString("word-service.host"),
        port = config.getInt("word-service.port"),
        baseUrl = config.getString("word-service.base-url"),
        requestTimeoutSeconds = config.getDouble("word-service.request-timeout-seconds")
      ),
      quiz = QuizRuntimeConfig(
        defaultSize = config.getInt("quiz.default-size")
      )
    )
  }
}
