[![Build Status](../../actions/workflows/CI.yml/badge.svg)](../../actions/workflows/CI.yml)

# Web Engineering 2025-2026 / Lab 3: Complete a Web API

A minimal Spring Boot + Kotlin starter for Lab 3. Complete the tasks in `docs/GUIDE.md` by filling in the SETUP/VERIFY blocks in the tests to demonstrate HTTP method safety and idempotency.

## Tech stack
- Spring Boot 3.5.3
- Kotlin 2.2.10
- Java 21 (toolchain)
- Gradle Wrapper

## Prerequisites
- Java 21
- Git

## Quick start
```bash
./gradlew clean build
./gradlew test
./gradlew bootRun
# Default: http://localhost:8080
```

## Project structure
- `src/main/kotlin/es/unizar/webeng/lab3`: application code (`Application.kt`, `Controller.kt`, `Employee.kt`, `EmployeeRepository.kt`, `OpenApiConfig.kt`)
- `src/main/resources`: application resources (`application.properties`)
- `src/test/kotlin/es/unizar/webeng/lab3`: tests (`ControllerTests.kt`, `IntegrationTests.kt`)
- `src/test/resources`: test resources (`application-test.properties`)
- `docs/GUIDE.md`: assignment instructions

## Assignment tasks
See `docs/GUIDE.md` for detailed steps:
- Complete the SETUP/VERIFY blocks for `POST`, `GET`, `PUT`, and `DELETE` in `ControllerTests`
- Run the test suite and ensure all tests pass

## Code quality and formatting
```bash
./gradlew ktlintFormat ktlintCheck
```

## Testing
```bash
./gradlew test
```

## OpenAPI/Swagger Documentation
### Access Points

#### Swagger UI (Interactive Documentation)

```
http://localhost:8080/swagger-ui.html
```

**Features:**

- Interactive API testing interface
- Try out API calls directly from browser
- View request/response examples
- Explore all endpoints and schemas

#### OpenAPI Specification (JSON)

```
http://localhost:8080/api-docs
```

**Use Cases:**

- Generate client SDKs
- Import into API testing tools (Postman, Insomnia)
- API contract testing

### Interactive Features

#### Try It Out
1. Start the application with `./gradlew bootRun`
2. Navigate to http://localhost:8080/swagger-ui.html
3. Expand any endpoint
4. Click **"Try it out"**
5. Modify parameters/body
6. Click **"Execute"**
7. View response

## Integration Tests

These tests use a real H2 in-memory database to verify the complete data flow from HTTP requests through the controller layer to database persistence.

### Running the Tests

#### Run Integration Tests Only (22 tests)

```bash
./gradlew test --tests "es.unizar.webeng.lab3.IntegrationTest"
```

#### Run All Tests

```bash
./gradlew test
```

#### View Test Reports

After running tests, open:
```
build/reports/tests/test/index.html
```

## OpenAPI Client Generation

The project includes tools to automatically generate a type-safe Kotlin client from the OpenAPI specification.

### Quick Start

```powershell
# 1. Start the application
.\gradlew bootRun

# 2. In another terminal, generate the client
.\gradlew generateClient
```

### Generated Files

The client code is generated in `build/generated-client/`:

```
build/generated-client/
├── src/main/kotlin/
│   └── es/unizar/webeng/lab3/client/
│       ├── api/
│       │   └── EmployeeManagementApi.kt        # API client with all endpoints
│       └── model/
│           └── Employee.kt                     # Data models
└── README.md                                   # Generated client documentation
```

### Using the Generated Client

The `EmployeeManagementApi` provides type-safe methods for all endpoints:

| Method                          | Description         | Endpoint                 |
| ------------------------------- | ------------------- | ------------------------ |
| `all()`                         | Get all employees   | `GET /employees`         |
| `one(id)`                       | Get employee by ID  | `GET /employees/{id}`    |
| `newEmployee(employee)`         | Create new employee | `POST /employees`        |
| `replaceEmployee(id, employee)` | Update employee     | `PUT /employees/{id}`    |
| `deleteEmployee(id)`            | Delete employee     | `DELETE /employees/{id}` |

### Example Usage

```kotlin
val api = EmployeeManagementApi(basePath = "http://localhost:8080")

// Create employee
val employee = Employee(id = null, name = "John Doe", role = "Developer")
val created = api.newEmployee(employee)

// Get all employees
val all = api.all()

// Update employee
val updated = Employee(id = created.id, name = "John Doe", role = "Senior Developer")
api.replaceEmployee(created.id!!, updated)

// Delete employee
api.deleteEmployee(created.id!!)
```

### Running Client Tests

```powershell
.\gradlew test --tests GeneratedClientExampleTest
```

See `GeneratedClientExampleTest.kt` for comprehensive examples demonstrating:

- Complete CRUD operations
- Error handling (404 responses)
- HTTP method idempotency testing
- Non-idempotency testing

## Bonus opportunities
Be the first to complete **at least two** of the following tasks to earn a bonus:

### 1. **Implement Comprehensive Security with Authentication and Authorization**
- **Description**: Add robust security mechanisms including JWT-based authentication, role-based authorization, and protection for different HTTP methods based on user permissions.
- **Implementation**: Implement JWT token generation/validation, create user roles (ADMIN, USER), secure endpoints that modify data (POST, PUT, DELETE) with proper authorization, and add security tests that verify unauthorized access is properly rejected.
- **Goal**: Demonstrate understanding of API security principles and protect sensitive operations.
- **Benefit**: Shows mastery of modern authentication/authorization patterns and API security best practices.

### 2. **Implement RESTful API Documentation with OpenAPI/Swagger** ✅
- **Description**: Add comprehensive API documentation using SpringDoc OpenAPI, including detailed descriptions of HTTP method semantics, request/response examples, and error scenarios.
- **Implementation**: Document all endpoints with proper HTTP status codes, include examples for safe vs unsafe operations, and add interactive Swagger UI.
- **Goal**: Create professional API documentation that explains the RESTful design principles.
- **Benefit**: Enhances API usability and demonstrates understanding of API design best practices.

### 3. **Add Advanced Error Handling with RFC 7807 Problem Details**
- **Description**: Implement sophisticated error handling using RFC 7807 Problem Details for HTTP APIs, with proper error types for different scenarios.
- **Implementation**: Create custom exception handlers, map different error scenarios to appropriate problem types, and include correlation IDs for debugging.
- **Goal**: Provide consistent, informative error responses that help API consumers understand and handle errors.
- **Benefit**: Shows understanding of modern API error handling standards and improves developer experience.

### 4. **Implement API Versioning and Content Negotiation**
- **Description**: Add API versioning support and content negotiation for different response formats (JSON, XML, etc.).
- **Implementation**: Support multiple API versions, handle different Accept headers, and provide appropriate responses based on client preferences.
- **Goal**: Demonstrate flexibility in API design and support for diverse client needs.
- **Benefit**: Shows understanding of API evolution and client compatibility strategies.

### 5. **Add Comprehensive Logging and Monitoring**
- **Description**: Implement structured logging with correlation IDs, request tracing, and performance metrics for API operations.
- **Implementation**: Add request/response logging, track method execution times, and include correlation IDs for request tracing.
- **Goal**: Enable effective debugging and monitoring of API behavior.
- **Benefit**: Demonstrates understanding of production-ready API observability.

### 6. **Implement Rate Limiting and API Protection**
- **Description**: Add sophisticated rate limiting, API key validation, and DDoS protection mechanisms to safeguard API endpoints from abuse.
- **Implementation**: Implement sliding window rate limiting, API key-based access control, request throttling, and protection against common attacks (brute force, spam).
- **Goal**: Protect the API from abuse, overuse, and malicious attacks.
- **Benefit**: Shows understanding of API protection strategies and production security concerns.

### 7. **Add Integration Tests with Real Database** ✅
- **Description**: Create comprehensive integration tests that use a real database (H2 or PostgreSQL) to test the complete data flow.
- **Implementation**: Test database transactions, verify data persistence, and test concurrent access scenarios.
- **Goal**: Ensure the API works correctly with real data persistence.
- **Benefit**: Demonstrates understanding of integration testing and database interactions.

### 8. **Implement Caching and Performance Optimization**
- **Description**: Add HTTP caching headers, implement response caching for safe operations, and optimize API performance.
- **Implementation**: Add appropriate cache headers for GET requests, implement response caching, and optimize database queries.
- **Goal**: Improve API performance and reduce server load.
- **Benefit**: Shows understanding of web performance optimization and caching strategies.

### 9. **Add API Testing with Contract Testing**
- **Description**: Implement contract testing using tools like Pact or Spring Cloud Contract to ensure API compatibility.
- **Implementation**: Define API contracts, test consumer-provider compatibility, and ensure backward compatibility.
- **Goal**: Ensure API contracts are maintained and consumers are not broken by changes.
- **Benefit**: Demonstrates understanding of API contract management and consumer protection.

### 10. **Replace with Spring WebFlux Reactive Implementation**
- **Description**: Replace the current blocking implementation with [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web-reactive.html) for reactive, non-blocking API operations.
- **Implementation**: Convert controllers to use reactive types (Mono/Flux), implement reactive repositories, and maintain the same API contract while gaining reactive benefits.
- **Goal**: Demonstrate understanding of reactive programming and non-blocking I/O.
- **Benefit**: Shows mastery of modern reactive web development and performance optimization.

Feel free to choose any of these bonus tasks to enrich your assignment. Completing them will not only earn you a bonus but also deepen your understanding of best practices in web development. 

If you need guidance on how to approach any of these tasks or have questions, don't hesitate to ask!
