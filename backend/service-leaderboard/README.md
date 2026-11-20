# smart-words / backend / service-leaderboard

[ BACKEND DOCS ](../README.md) |
[ FRONTEND DOCS ](../../frontend/README.md) |
[ MAIN DOCS ](../../README.md)

Service for storing and exposing quiz leaderboard results.

It accepts score submissions for selected quiz modes and stores full history in JSON data file.
Then it provides top-ranked results for each mode based on:

1. higher percentage score first,
2. when percentage ties, higher total question count first,
3. when still tied, older entry first.

### endpoints

* `GET /health` -> `RET: OK 200+TEXT` | `ERR 500+TEXT`<br>Check leaderboard service health status
* `POST /leaderboard/entries + JSON` -> `RET: CREATED 201 + Entry JSON` | `ERR 400`<br>Submit new leaderboard entry
* `GET /leaderboard/entries/{game}/{mode}?limit=10` -> `RET: OK 200 + Entries JSON` | `ERR 400`<br>Read ranked leaderboard entries for selected game and mode

### submit payload

```json
{
  "game": "quiz",
  "mode": 0,
  "username": "player",
  "scorePercent": 0.8,
  "totalQuestions": 10,
  "correctAnswers": 8
}
```

### technology

<img src="../../resources/logo/scala.png" alt="scala logo" width="300"/>

This service is written entirely in **Scala** language.<br>
No external database is used. Runtime data is persisted in JSON file.

### configuration

Runtime settings are read from `src/main/resources/application.conf` with optional environment variable overrides.

Most important keys:

* `service.name` (`LEADERBOARD_SERVICE_NAME`)
* `service.host` (`LEADERBOARD_SERVER_HOST`)
* `service.port` (`LEADERBOARD_SERVER_PORT`)
* `service.idle-timeout-minutes` (`LEADERBOARD_SERVER_IDLE_TIMEOUT_MINUTES`)
* `cors.any-origin` (`LEADERBOARD_CORS_ANY_ORIGIN`)
* `cors.allow-credentials` (`LEADERBOARD_CORS_ALLOW_CREDENTIALS`)
* `cors.max-age-seconds` (`LEADERBOARD_CORS_MAX_AGE_SECONDS`)
* `cors.any-method` (`LEADERBOARD_CORS_ANY_METHOD`)
* `data.file` (`LEADERBOARD_DATA_FILE`)
* `data.dir` (`LEADERBOARD_DATA_DIR`)

---
<p align="center">Created by PNK with 💚 @ 2026</p>
