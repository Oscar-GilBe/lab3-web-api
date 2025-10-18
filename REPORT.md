# Lab 3 Complete a Web API -- Project Report

## Description of Changes

### 1. Completion of Unit Tests in ControllerTests.kt

The first significant change involved **completing the implementation of unit tests** in the `ControllerTests.kt` file. This work focused on proper mock setup using the `every` keyword from MockK library to define the behavior of mocked dependencies, specifically the `EmployeeRepository`. Each test case was structured to first configure the mock responses, then simulate HTTP requests through MockMvc, and finally verify that the expected repository methods were called using the `verify` keyword. This approach allowed for isolated testing of the controller logic without requiring a real database connection.

Through this testing process, important insights about HTTP method semantics were discovered. The tests revealed which operations are idempotent and which are not, as well as which operations are considered safe according to REST principles. For instance, GET requests were confirmed to be both safe and idempotent since they don't modify server state and produce the same result when repeated. PUT requests demonstrated idempotency because updating a resource with the same data multiple times results in the same final state. In contrast, POST requests were shown to be non-idempotent as each invocation creates a new resource with a different identifier. DELETE operations proved to be idempotent since deleting an already deleted resource produces the same outcome. These practical observations reinforced theoretical knowledge about RESTful API design.

### 2. OpenAPI Configuration and Documentation

The second major enhancement was the **addition of comprehensive OpenAPI documentation** through SpringDoc OpenAPI integration. A new configuration file `OpenApiConfig.kt` was created to define the API metadata, including title, version, description, and licensing information. More importantly, this configuration bean serves as the central place for documenting RESTful design principles and HTTP method semantics that apply to the entire API.

The `Controller.kt` file was extensively annotated with OpenAPI annotations to provide detailed documentation for each endpoint. Every operation includes comprehensive descriptions explaining not only what the endpoint does but also its HTTP properties such as whether it's safe or unsafe, idempotent or non-idempotent. Multiple response codes are documented with example scenarios, and request bodies are annotated with schema information and examples. The `Employee.kt` entity class was also enhanced with schema annotations describing each field, providing examples, and documenting constraints. Finally, the `application.yml` configuration file was updated to customize the Swagger UI presentation and enable the interactive API documentation interface at `/swagger-ui.html`.

### 3. Comprehensive Integration Tests

The third major change was the **implementation of a complete integration test suite** in `IntegrationTest.kt` to verify the correct functioning of the database layer and the entire application stack. Unlike other tests that mock dependencies, these integration tests use a real H2 in-memory database configured through `application-test.yml`. The test suite includes twenty-two test cases organized into six logical categories: basic CRUD operations, database transaction tests, data persistence tests, concurrent access tests, API response validation, and edge cases and error handling.

These integration tests make actual HTTP requests using `TestRestTemplate` and verify that data is correctly persisted by querying the database directly through `EmployeeRepository`. This approach validates the complete request-response cycle including JSON serialization, controller processing, service layer logic, JPA entity management, and database storage. The tests also verify proper transaction management, optimistic locking behavior, concurrent access handling, and edge cases such as empty databases or very long field values. This comprehensive testing strategy provides high confidence that the API functions correctly under real-world conditions.

### 4. CI workflow:

A GitHub Actions workflow is configured to automatically run all tests on every commit. This CI process ensures continuous verification of application integrity and prevents regressions. **Test results are saved as downloadable artifacts**.

---

## Technical Decisions

Throughout the development of this project, several important technical decisions were made regarding testing strategies, tools, and documentation approaches:

For the integration testing infrastructure, **H2 in-memory database** was selected as the testing database. This choice provides fast test execution without requiring external database servers or Docker containers, making the tests runnable in any environment without additional setup. The H2 database is configured with `DB_CLOSE_DELAY=-1` to keep the database alive between connections within a test, while the `ddl-auto: create-drop` setting ensures a clean state for each test run by recreating the schema. This configuration balances the need for realistic database testing with the practical requirement for fast, repeatable tests.

When choosing between `TestRestTemplate` and `MockMvc` for **integration tests**, `TestRestTemplate` was selected because it makes real HTTP requests through the entire application stack. This approach tests the complete request-response cycle including JSON serialization and deserialization, which `MockMvc` does not fully exercise since it operates at the servlet layer.

**Test isolation** was achieved through a multi-layered strategy. Each test class uses `@BeforeEach` to clear the database before each test method, ensuring no data pollution between tests. A separate test profile activates test-specific configuration, and tests run on a random server port to avoid conflicts. The in-memory database is recreated for each test run, providing complete isolation. This strategy eliminates test interdependencies and produces predictable, repeatable results regardless of execution order.

The integration tests employ direct database verification by injecting `EmployeeRepository` alongside the HTTP client. After making HTTP requests, tests query the **repository directly** to confirm that data was actually persisted to the database, not just returned in HTTP responses. This pattern validates the entire stack from HTTP layer through JPA to database storage, testing that the Hibernate/JPA layer correctly manages entities and that database consistency is maintained.

For **API documentation**, the documentation approach follows a code-first strategy using annotations directly in the source code. This ensures that documentation stays in sync with implementation since they exist in the same files. When code is refactored, the documentation annotations move with it, preventing the documentation drift that occurs with external documentation files.

**Interactive documentation through Swagger UI** was enabled to provide self-service API exploration. Developers can try operations directly from their browser without writing code, getting instant feedback on how the API behaves. This significantly improves developer experience by eliminating the need for separate API clients during initial exploration and testing phases.

Finally, **field-level schema documentation** was added using `@Schema` annotations on the `Employee` entity. This provides clear data model documentation showing type information, constraints, and examples for each field.

---

## Learning Outcomes

Working on this project provided numerous valuable learning experiences across multiple areas of software engineering, from Kotlin language features to testing strategies and API documentation practices.

One of the first important discoveries was the use of **type aliases in Kotlin**, specifically applied in the `Controller.kt` file where `RequestBody` is imported. Using the `import` statement with an alias allows for clear disambiguation. This prevents naming conflicts and makes the code more readable by explicitly showing which `RequestBody` class is being used in each context.

Another significant learning point involved the use of **Markdown formatting** within the descriptions of the `OpenApiConfig.kt` file. The OpenAPI specification supports Markdown in description fields, which allows for rich formatting including headers, bold text, and lists. The Swagger UI automatically renders this Markdown, providing an enhanced visual experience for API consumers.

Understanding the differences between **parameters and request bodies** across various HTTP operations was crucial. GET and DELETE operations typically use path parameters or query parameters to identify resources, while POST and PUT operations send data in the request body. This distinction reflects the RESTful principle that GET requests should not have side effects and should only retrieve data based on URL parameters, whereas POST and PUT modify server state and require complex data structures that are better suited for request bodies. The OpenAPI annotations make these differences explicit and help developers understand how to correctly use each endpoint.

The project also demonstrated the power of Kotlin's **raw strings** through the use of `val newEmployee` declarations in `IntegrationTest.kt`. Raw strings, delimited by triple quotes, allow for writing multi-line JSON or other formatted text without needing to escape special characters. This makes test data more readable and easier to maintain, especially when dealing with complex JSON structures that need to be sent in HTTP request bodies.

The **test organization strategy** employed `@Nested` inner classes to **group related test cases**. This pattern provides logical grouping that improves test report organization and makes the test structure clearer. When viewing test results, the nested structure immediately shows which category of functionality is being tested, whether it's basic CRUD operations, concurrent access, or edge cases.

For **concurrent testing scenarios**, a combination of `CompletableFuture` for asynchronous operations, `Executor` with thread pools for managing concurrent execution, and `CountDownLatch` for synchronization was implemented. This approach allows testing real-world concurrent access patterns that would occur when multiple clients access the API simultaneously. These tests verify that the locking mechanism in JPA works correctly under concurrent load.

Working with optional types led to learning about the `isPresent` method for checking whether an `Optional` value contains a non-null value. This is particularly useful when working with repository methods that return `Optional<Employee>` instead of nullable types. Using `isPresent` before accessing the value with `get()` prevents null pointer exceptions.

An interesting Kotlin feature discovered during assertion writing was the use of the **spread operator with arrays**. In the assertion `assertThat(response.body!!.map { it.name }).containsExactlyInAnyOrder(*employees.map { it.name }.toTypedArray())`, the asterisk operator spreads the array elements as individual arguments to the method. This is necessary because `containsExactlyInAnyOrder` expects varargs (a list of separate values) rather than a single array parameter. Without the spread operator, the assertion would fail to compile or would compare the array object itself instead of its contents.

The `all` function in Kotlin collections proved invaluable for verifying that every element in a collection satisfies a condition. The assertion `assertThat(results.all { it != null && it.id != null }).isTrue()` demonstrates checking that all concurrent operation results are non-null and have valid IDs. This is more concise and expressive than writing a loop to check each element individually, and it clearly communicates the intent that every single element must meet the criteria.

Finally, understanding the difference between `var` and `val` became particularly important in the "should handle mixed concurrent operations" test. The `val` keyword declares immutable references that **cannot be reassigned after initialization**, while `var` declares **mutable references that can be changed**.

---

## AI Disclosure

### AI Tools Used

ChatGPT

### AI-Assisted Work

AI was used for:

* Initial test structure templates for integration test suite
* Guidance on different approaches for concurrent testing implementations
* Documentation structure and annotation patterns for OpenAPI
* OpenAPI configuration class structure and comprehensive descriptions and explanations for each HTTP method
* Improving the clarity, structure, and consistency of this `report.md` document by providing an initial draft and editorial suggestions.

The AI assistance primarily focused on generating initial scaffolding and organization patterns that were later customized and expanded to meet specific project requirements. For concurrent testing scenarios, multiple strategies for handling thread synchronization and managing concurrent HTTP requests were suggested, allowing for informed decision-making about which patterns best suited the project's needs.

For the OpenAPI documentation, AI assistance was instrumental in establishing the documentation structure with detailed explanations of idempotency, safety properties, and RESTful principles that were then refined and adapted to match the specific implementation details of this API.

**Percentage of AI-assisted vs. original work**: Approximately **45%** of the work was AI-assisted. This mainly included early drafts of test skeletons and documentation for OpenAPI.

**Any modifications made to AI-generated code**: All AI-generated content (test skeletons, configurations, and report sections) was thoroughly reviewed, adapted, and modified to ensure accuracy, alignment with project requirements, and compliance with clean coding practices. Nothing was used without verification and adjustment to fit the project’s goals.

### Original Work

The completion of unit tests in `ControllerTests.kt` was developed entirely without AI assistance, representing independent work in implementing proper mock setup with MockK, verification patterns, and understanding of HTTP method semantics through practical testing. The refinement and development of the integration tests were also carried out through my own work. While the AI initially provided basic test skeletons, I performed substantial enhancements to create robust, comprehensive, and realistic test scenarios. This included expanding the initial structures with detailed CRUD, persistence, concurrency, and API response validation tests that accurately reflect real-world behavior and ensure full coverage of the application logic.

The analysis and understanding phase involved independently studying the codebase provided by the professor to comprehend the existing architecture, design patterns, and implementation choices. This included examining the Controller structure and Employee entity relationships to understand how the application components interact.

My learning process was based on studying the provided codebase and reading key sections of the Baeldung tutorial ["Documenting a Spring REST API Using OpenAPI 3.0"](https://www.baeldung.com/spring-rest-openapi-documentation), which provided foundational knowledge about SpringDoc OpenAPI integration and annotation usage patterns. This self-directed learning enabled understanding of how to properly document RESTful APIs according to standards and best practices.
