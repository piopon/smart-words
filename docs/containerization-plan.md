# smart-words containerization plan

## purpose

This document defines the target container architecture for smart-words and a safe migration path from manual local startup to one-command deployment with Compose.

## current state summary

- Frontend is static HTML/CSS/JS opened directly from files.
- Backend has two Scala services:
  - service-word on port 1111.
  - service-quiz on port 2222.
- Frontend currently calls hardcoded localhost backend URLs.
- service-quiz currently calls service-word via a hardcoded localhost URL.
- Dictionary and mode JSON data are currently read/written from runtime resource paths.

## target state summary

Run three containers with Docker Compose:

1. frontend-nginx
- Serves static frontend files.
- Reverse-proxies API requests to backend containers.
- Exposes a single host port for users.

2. service-word
- Scala runtime container for dictionary CRUD API.
- Persists dictionaries to a mounted host volume.

3. service-quiz
- Scala runtime container for quiz API.
- Calls service-word over Compose internal network.
- Persists mode data to a mounted host volume.

## architecture decisions

### decision 1: use an nginx container for frontend

Why:
- Static assets need a production-like web server in homelab.
- Reverse proxy gives one browser origin, reducing CORS and URL complexity.

Result:
- Browser uses one base URL only (frontend host).
- Backend APIs are reached via proxy paths.

### decision 2: keep two backend containers

Why:
- Matches current service boundaries and code ownership.
- Keeps migration risk low and avoids unnecessary merge of concerns.

Result:
- service-word and service-quiz remain independently deployable.

### decision 3: internal service DNS via Compose network

Why:
- Containers should communicate by service names, not localhost.

Result:
- service-quiz reaches service-word at a configurable endpoint like http://service-word:1111.

### decision 4: move mutable JSON data to mounted volumes

Why:
- Container filesystems are ephemeral.
- User dictionary and mode edits must survive restarts and image updates.

Result:
- Dedicated bind mounts or named volumes for persistent data.

### decision 5: build once, run slim runtime images

Why:
- Build tools are heavy and should not be in runtime image.
- Smaller images improve pull/start time on homelab nodes.

Result:
- Multi-stage Dockerfiles: build stage (sbt/JDK), runtime stage (JRE + packaged app).

## proposed request routing

- Browser requests pages and assets from frontend-nginx.
- Browser calls:
  - /api/word/* -> proxied to service-word:1111
  - /api/quiz/* -> proxied to service-quiz:2222
- service-quiz calls service-word through internal endpoint configured by environment variable.

## persistence model

### service-word data

- Persist dictionary JSON files in a writable data directory mounted from host.
- Seed initial built-in dictionaries on first run if target data directory is empty.

### service-quiz data

- Persist modes JSON in a writable data directory mounted from host.
- Seed initial modes file on first run if target file does not exist.

## configuration model

Use environment variables with safe defaults:

- QUIZ_WORD_SERVICE_URL
  - Endpoint used by service-quiz to call service-word.
  - Local default remains current localhost behavior for non-container runs.

- WORD_DATA_DIR
  - Writable directory for dictionary JSON files.

- QUIZ_DATA_DIR
  - Writable directory for modes JSON file.

## deployment model

Compose file responsibilities:

- Define services, ports, networks, volumes, restart policies.
- Build images from local Dockerfiles or use prebuilt tags.
- Start order using healthchecks and depends_on conditions.

## security and ops baseline

Initial baseline for homelab:

- Expose only frontend port publicly.
- Keep backend ports internal unless explicitly needed for debugging.
- Add healthchecks for all services.
- Set restart policy to unless-stopped.

Future optional hardening:

- Read-only root filesystem where possible.
- Non-root runtime user in each image.
- Pinned image digests.

## rollout sequence

1. Make backend URLs and data locations configurable.
2. Add packaging for standalone backend runtime artifacts.
3. Add Dockerfiles for both backend services.
4. Add nginx frontend image and API proxy config.
5. Add docker-compose.yml with volumes and healthchecks.
6. Validate end-to-end flows and persistence across restarts.

## acceptance criteria for containerized deployment

- One command starts full stack.
- Frontend is reachable in browser from homelab host.
- Quiz flow works end-to-end.
- Word CRUD works and persists after restart.
- Mode updates persist after restart.
- service-quiz successfully communicates with service-word without localhost assumptions.
