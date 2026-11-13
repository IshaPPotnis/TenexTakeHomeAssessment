# TenexTakeHomeAssessment

A Java-based Spring Boot REST API with a small JavaScript interface for submitting log entries.

## Features
- Spring Boot 3.x
- Java 17
- REST API for log processing
- Redis-backed parser definition CRUD API
- Browser UI for pasting log entries or loading a text file
- Swagger/OpenAPI documentation
- H2 in-memory database for local development

## Project Structure
```text
src/main/java/       Spring Boot API
src/main/resources/  Application configuration
ui/                  JavaScript logs interface and Dockerfile
```

## Run the application
```bash
./mvnw spring-boot:run
```

## API Endpoints
- POST `/api/logs/process` - Accepts log strings and the parser ID to use
- GET `/api/definitions` - List parser definitions
- GET `/api/definitions/{parserId}` - Get a parser definition
- POST `/api/definitions` - Create a parser definition
- PUT `/api/definitions/{parserId}` - Update a parser definition
- DELETE `/api/definitions/{parserId}` - Delete a parser definition

Start Redis locally before using the definitions API:
```bash
docker run --rm --name tenex-redis -p 6379:6379 redis:7-alpine
```

The API connects to Redis at `localhost:6379` by default. Definition requests use this shape:
```json
{
  "parserId": 1,
  "parserName": "XML parser",
  "parserType": "XML"
}
```

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

Open <http://localhost:3000>. The UI container proxies log submissions to the API at `http://host.docker.internal:8080`; override `API_BASE_URL` if the API runs elsewhere.

## Swagger UI
Open:
```text
http://localhost:8080/swagger-ui.html
```

## H2 Console
Open:
```text
http://localhost:8080/h2-console
```

Use the JDBC URL:
```text
jdbc:h2:mem:tenexdb
```

## Sample Request
```json
{
  "firstName": "Alice",
  "lastName": "Warren",
  "email": "alice.warren@tenex.com",
  "department": "Engineering",
  "salary": 110000.00
}
```
