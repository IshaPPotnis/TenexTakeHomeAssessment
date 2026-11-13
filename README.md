# TenexTakeHomeAssessment

A Java-based Spring Boot REST API with a small JavaScript interface for submitting log entries.

## Features
- Spring Boot 3.x
- Java 17
- REST API for log processing
- Redis-backed parser definition CRUD API
- Browser UI for processing logs, managing parser definitions, and maintaining detection rules
- Session-based demo login with customer-scoped parser definitions and detection rules
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
- POST `/api/auth/login` - Authenticate and create a customer session
- GET `/api/auth/session` - Get the customer associated with the bearer token
- POST `/api/auth/logout` - Revoke the current bearer token
- POST `/api/logs/process` - Accepts log strings and the parser ID to use
- GET `/api/definitions` - List parser definitions
- GET `/api/definitions/{parserId}` - Get a parser definition
- POST `/api/definitions` - Create a parser definition
- PUT `/api/definitions/{parserId}` - Update a parser definition
- DELETE `/api/definitions/{parserId}` - Delete a parser definition
- GET `/api/detection-rules` - List detection rules
- GET `/api/detection-rules/{id}` - Get a detection rule
- POST `/api/detection-rules` - Create a detection rule
- PUT `/api/detection-rules/{id}` - Update a detection rule
- DELETE `/api/detection-rules/{id}` - Delete a detection rule

The API connects to Redis at `localhost:6379` by default. Parser IDs are assigned incrementally by Redis, so create and update requests provide the name and type, plus an optional list of fields to remove:
```json
{
  "parserName": "XML parser",
  "parserType": "XML",
  "fieldsToRemove": ["password", "token"]
}
```
`fieldsToRemove` is optional. Matching keys are removed recursively from the parsed JSON output; omitted or empty lists leave the parsed output unchanged.

On startup, the API creates any missing sample definitions (`sample xml parser`, `sample json parser`, and `sample csv parser`) in Redis. Existing sample definitions are preserved.

On startup, the API also creates the `Failed Login attempt` detection rule if it does not already exist. It matches `EventID=4625 AND Channel=Security`; its four-digit ID is assigned by Redis.

Detection rule IDs are generated incrementally as four-digit numbers. Create and update requests use this shape:
```json
{
  "name": "High error rate",
  "ruleLogic": "level EQUAL ERROR",
  "description": "Detects error-level log entries."
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
      "notes": "Parsed successfully using XML.",
      "matchedIncident": []
    }
  ]
}
```

Each log entry is parsed according to the selected definition's `parserType` (`XML`, `JSON`, or `CSV`). CSV entries are returned as JSON arrays of field values. `matchedIncident` contains each matching rule as an `id-Name` string, or an empty list when no rule matches. Parsed output is displayed as a field/value table in the UI.

The UI's Detection library tab supports listing, creating, updating, and deleting detection rules through `/api/detection-rules`.

## Run the UI
Start the Spring Boot API on port 8080, then build and run the standalone UI container:
```bash
docker build -t logs-processor-ui ./ui
docker run --rm --add-host=host.docker.internal:host-gateway -p 3000:3000 logs-processor-ui
```

Open <http://localhost:3000>. The UI container proxies requests to the Spring API at `http://host.docker.internal:8080`; override `API_BASE_URL` if the API runs elsewhere. The Spring API must be running and reachable from the UI container. For Docker on Linux, `--add-host=host.docker.internal:host-gateway` makes the host API address available inside the UI container.

Sign in with username `admin-dev` and password `admin@123`. Login returns a random bearer token and `customerName`; the UI stores both for the current browser tab and sends `Authorization: Bearer <token>` and `X-Customer-Name: <customerName>` on subsequent requests until logout. The API verifies that the customer-name header matches the customer bound to the bearer token. Tokens expire after eight hours and are stored by SHA-256 digest in Redis. This hardcoded account is for local development only and maps to the `admin-dev` customer scope. Parser definitions and detection rules are stored with a `customerName`, assigned by the authenticated token rather than accepted from API request bodies. A customer sees its own records and records in the `GLOBAL` scope. Global records are read-only in the customer UI. The built-in sample parsers and failed-login rule are global. Do not use the demo credentials in production.

All parser, detection-rule, and log-processing API routes require the bearer token and matching `X-Customer-Name` header returned by `/api/auth/login`. `/api/auth/session` and `/api/auth/logout` also require both; only the login route is accessible without authentication.

## Swagger UI
Open:
```text
http://localhost:8080/swagger-ui.html
```
