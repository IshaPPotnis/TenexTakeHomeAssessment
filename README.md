# TenexTakeHomeAssessment

A Java-based Spring Boot REST API with a small JavaScript interface for submitting log entries.

## Features
- Spring Boot 3.x
- Java 17
- REST API for log processing
- Redis-backed parser definition CRUD API
- Browser UI for processing logs and managing parser definitions
- Swagger/OpenAPI documentation

## Project Structure
```text
src/main/java/       Spring Boot API
src/main/resources/  Application configuration
ui/                  JavaScript logs interface and Dockerfile
```

## Run the application
Start Redis first, then run the Spring API:
```bash
docker run --rm --name tenex-redis -p 6379:6379 redis:7-alpine
./mvnw spring-boot:run
```

## API Endpoints
- POST `/api/logs/process` - Accepts log strings and the parser ID to use
- GET `/api/definitions` - List parser definitions
- GET `/api/definitions/{parserId}` - Get a parser definition
- POST `/api/definitions` - Create a parser definition
- PUT `/api/definitions/{parserId}` - Update a parser definition
- DELETE `/api/definitions/{parserId}` - Delete a parser definition

The API connects to Redis at `localhost:6379` by default. Parser IDs are assigned incrementally by Redis, so create and update requests only provide the name and type:
```json
{
  "parserName": "XML parser",
  "parserType": "XML"
}
```

On startup, the API creates any missing sample definitions (`sample xml parser`, `sample json parser`, and `sample csv parser`) in Redis. Existing sample definitions are preserved.

Example request:
```json
{
  "logs": ["INFO Application started", "WARN Cache nearing capacity"],
  "parserId": 1
}
```

The response contains one result per log entry:
```json
{
  "results": [
    {
      "log": "INFO Application started",
      "parsedOutput": "{\"Application started\":\"INFO\"}",
      "notes": "Parsed successfully using XML."
    }
  ]
}
```

Each log entry is parsed according to the selected definition's `parserType` (`XML`, `JSON`, or `CSV`). CSV entries are returned as JSON arrays of field values.

## Run the UI
Start the Spring Boot API on port 8080, then build and run the standalone UI container:
```bash
docker build -t logs-processor-ui ./ui
docker run --rm --add-host=host.docker.internal:host-gateway -p 3000:3000 logs-processor-ui
```

Open <http://localhost:3000>. The UI container proxies requests to the Spring API at `http://host.docker.internal:8080`; override `API_BASE_URL` if the API runs elsewhere. The Spring API must be running and reachable from the UI container. For Docker on Linux, `--add-host=host.docker.internal:host-gateway` makes the host API address available inside the UI container.

## Swagger UI
Open:
```text
http://localhost:8080/swagger-ui.html
```
