# smart-words Docker Compose runbook

## prerequisites

- Docker Engine or Docker Desktop with Compose v2.
- Ports used on host:
  - 8080 (frontend)

## first run

From repository root:

```powershell
docker compose up --build -d
```

Open application:

- http://localhost:8080

## verify stack

```powershell
docker compose ps
docker compose logs -f frontend
docker compose logs -f service-word
docker compose logs -f service-quiz
```

Health endpoints through internal network:

- service-word: `GET /health` on port 1111
- service-quiz: `GET /health` on port 2222

## backend runtime configuration

The default backend runtime settings are defined in:

- [backend/service-word/src/main/resources/application.conf](../backend/service-word/src/main/resources/application.conf)
- [backend/service-quiz/src/main/resources/application.conf](../backend/service-quiz/src/main/resources/application.conf)

Docker Compose overrides selected values via environment variables in [docker-compose.yml](../docker-compose.yml).

Most commonly adjusted variables:

- service-word: `WORD_SERVICE_NAME`, `WORD_SERVER_HOST`, `WORD_SERVER_PORT`, `WORD_DATA_DIR`, `WORD_SEED_DIR`, `WORD_DICTIONARY_EXTENSION`
- service-quiz: `QUIZ_SERVICE_NAME`, `QUIZ_SERVER_HOST`, `QUIZ_SERVER_PORT`, `QUIZ_DATA_DIR`, `QUIZ_SEED_DIR`, `QUIZ_MODE_FILE`, `QUIZ_DEFAULT_SIZE`, `QUIZ_DEFAULT_MODE`, `QUIZ_DEFAULT_LANGUAGE`, `QUIZ_WORD_SERVICE_NAME` (fallback: `WORD_SERVICE_NAME`), `WORD_SERVICE_HOST`, `WORD_SERVICE_PORT`, `QUIZ_WORD_SERVICE_URL`, `QUIZ_WORD_SERVICE_TIMEOUT_SECONDS`

## automated smoke test

After stack startup, run:

```powershell
./scripts/smoke-test.ps1
```

What this validates:

- frontend root responds with `200`
- proxied `/api/word/health` and `/api/quiz/health` are healthy
- modes API can create/read/delete data
- mode persistence survives `service-quiz` restart

## persistence

Data is persisted in host directories:

- `./data/service-word` -> dictionaries JSON files
- `./data/service-quiz` -> modes JSON file

Those folders are seeded from bundled defaults on first run when empty.

## stop and cleanup

Stop services:

```powershell
docker compose down
```

Stop and remove persisted data too:

```powershell
docker compose down
Remove-Item -Recurse -Force .\data\service-word, .\data\service-quiz
```

## rebuild after changes

```powershell
docker compose up --build -d
```
