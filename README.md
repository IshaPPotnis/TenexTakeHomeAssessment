# TenexTakeHomeAssessment

A Java-based Spring Boot REST API project with clean layering and a production-style structure.

## Features
- Spring Boot 3.x
- Java 17
- REST API for employee management
- Service layer abstraction
- Repository layer with JPA
- Validation and global exception handling
- Swagger/OpenAPI documentation
- H2 in-memory database for local development

## Project Structure
```text
src/
  main/
    java/
      com/tenextakehome/
        config/
        controller/
        dto/
        exception/
        model/
        repository/
        service/
          impl/
        DataSeeder.java
        TenexTakeHomeAssessmentApplication.java
    resources/
      application.properties
```

## Run the application
```bash
./mvnw spring-boot:run
```

## API Endpoints
- GET `/api/employees` - Get all employees
- GET `/api/employees/{id}` - Get employee by ID
- POST `/api/employees` - Create employee
- PUT `/api/employees/{id}` - Update employee
- DELETE `/api/employees/{id}` - Delete employee

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
