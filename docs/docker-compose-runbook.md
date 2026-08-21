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
