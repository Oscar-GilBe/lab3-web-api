package es.unizar.webeng.lab3

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import io.swagger.v3.oas.annotations.parameters.RequestBody as DocRequestBody

@RestController
@Tag(
    name = "Employee management",
    description =
        "RESTful API endpoints for employee CRUD operations",
)
class EmployeeController(
    private val repository: EmployeeRepository,
) {
    @GetMapping("/employees")
    @Operation(
        summary = "Retrieve all employees",
        description = """
            HTTP Method: GET
            
            PROPERTIES:
            - Safe: Does not modify server state
            - Idempotent: Multiple identical requests produce the same result
            
            BEHAVIOR:
            Retrieves a list of all employee resources from the database. This is a read-only 
            operation that does not modify any data. Multiple calls will return the same result 
            (unless data is modified by other operations).
            
            USE CASE:
            Use this endpoint to get a complete listing of all employees, for example to display 
            in a dashboard or export to a report.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved all employees",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = Array<Employee>::class),
                        examples = [
                            ExampleObject(
                                name = "Multiple employees",
                                value = """[
                                    {
                                        "name": "John Doe",
                                        "role": "Developer",
                                        "id": 1
                                    },
                                    {
                                        "name": "Jane Smith",
                                        "role": "Manager",
                                        "id": 2
                                    }
                                ]""",
                            ),
                            ExampleObject(
                                name = "Empty list",
                                value = "[]",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun all(): Iterable<Employee> = repository.findAll()

    @PostMapping("/employees")
    @Operation(
        summary = "Create a new employee",
        description = """
            HTTP Method: POST
            
            PROPERTIES:
            - Not Safe: Modifies server state by creating a resource
            - Not Idempotent: Each request creates a new employee with a unique ID
            
            BEHAVIOR:
            Creates a new employee resource in the database. Each request generates a new unique 
            ID, even if the employee data is identical. This demonstrates non-idempotent behavior 
            (calling this endpoint twice with the same data creates two distinct employees).
            
            RESPONSE:
            Returns HTTP 201 (Created) with a Location header pointing to the new resource.
            
            USE CASE:
            Use this endpoint when adding a new employee to the system. The server assigns the ID.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Employee successfully created. Location header contains URI of new resource.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = Employee::class),
                        examples = [
                            ExampleObject(
                                name = "Created employee",
                                value = """
                                {
                                    "name": "John Doe",
                                    "role": "Developer",
                                    "id": 1
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun newEmployee(
        @DocRequestBody(
            description = "Employee data to create. ID will be auto-generated.",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [
                        ExampleObject(
                            name = "New developer",
                            value = """
                            {
                                "name": "John Doe",
                                "role": "Developer"
                            }
                            """,
                        ),
                        ExampleObject(
                            name = "New manager",
                            value = """
                            {
                                "name": "Jane Smith",
                                "role": "Manager"
                            }
                            """,
                        ),
                    ],
                ),
            ],
        )
        @Valid
        @RequestBody
        newEmployee: Employee,
    ): ResponseEntity<Employee> {
        val employee = repository.save(newEmployee)
        val location =
            ServletUriComponentsBuilder
                .fromCurrentServletMapping()
                .path("/employees/{id}")
                .build(employee.id)
        return ResponseEntity.created(location).body(employee)
    }

    @GetMapping("/employees/{id}")
    @Operation(
        summary = "Retrieve a specific employee by ID",
        description = """
            HTTP Method: GET
            
            PROPERTIES:
            - Safe: Does not modify server state
            - Idempotent: Multiple identical requests produce the same result
            
            BEHAVIOR:
            Retrieves a single employee resource by its unique identifier. This is a read-only 
            operation. Multiple calls with the same ID will return the same employee data 
            (unless modified by other operations).
            
            ERROR HANDLING:
            Returns HTTP 404 (Not Found) if no employee exists with the specified ID.
            
            USE CASE:
            Use this endpoint to retrieve details of a specific employee, for example to display 
            on an employee profile page.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Employee found and returned successfully",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = Employee::class),
                        examples = [
                            ExampleObject(
                                name = "Found employee",
                                value = """
                                {
                                    "name": "John Doe",
                                    "role": "Developer",
                                    "id": 1
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Employee not found with the specified ID",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(
                                name = "Not found error",
                                value = """
                                {
                                    "timestamp": "2025-10-14T19:30:00.000+00:00",
                                    "status": 404,
                                    "error": "Not Found",
                                    "path": "/employees/999"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun one(
        @Parameter(
            description = "Unique identifier of the employee",
            required = true,
            example = "1",
        )
        @PathVariable
        id: Long,
    ): Employee = repository.findById(id).orElseThrow { EmployeeNotFoundException(id) }

    @PutMapping("/employees/{id}")
    @Operation(
        summary = "Update or create an employee",
        description = """
            HTTP Method: PUT
            
            PROPERTIES:
            - Not Safe: Modifies server state
            - Idempotent: Multiple identical requests produce the same final state
            
            BEHAVIOR:
            Updates an existing employee or creates a new one if the ID doesn't exist. 
            This demonstrates idempotent behavior, calling this endpoint multiple times with 
            the same data results in the same final state (one employee with the specified data).
            
            THREE SCENARIOS:
            1. Update (200 OK): If employee exists, updates their data
            2. Create (201 Created): If employee doesn't exist, creates new employee
            3. Internal server error (500): If there is an unexpected error during processing
            
            RESPONSE:
            - HTTP 200 (OK) if existing employee was updated
            - HTTP 201 (Created) if new employee was created
            - HTTP 500 (Internal Server Error) for unexpected errors
            - Content-Location header contains URI of the resource
            
            USE CASE:
            Use this endpoint to update employee information or create an employee
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Existing employee successfully updated",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = Employee::class),
                        examples = [
                            ExampleObject(
                                name = "Updated employee",
                                value = """
                                {
                                    "name": "John Doe Updated",
                                    "role": "Senior Developer",
                                    "id": 1
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "201",
                description = "New employee created",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = Employee::class),
                        examples = [
                            ExampleObject(
                                name = "Created employee",
                                value = """
                                {
                                    "name": "New Employee",
                                    "role": "Analyst",
                                    "id": 10
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error occurred",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(
                                name = "Internal server error",
                                value = """
                                {
                                    "timestamp": "2025-10-14T19:30:00.000+00:00",
                                    "status": 500,
                                    "error": "Internal Server Error",
                                    "path": "/employees/1"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun replaceEmployee(
        @DocRequestBody(
            description = "Updated employee data",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [
                        ExampleObject(
                            name = "Update to senior role",
                            value = """
                            {
                                "name": "John Doe",
                                "role": "Senior Developer"
                            }
                            """,
                        ),
                        ExampleObject(
                            name = "Promotion to manager",
                            value = """
                            {
                                "name": "Jane Smith",
                                "role": "Engineering Manager"
                            }
                            """,
                        ),
                    ],
                ),
            ],
        )
        @RequestBody
        newEmployee: Employee,
        @Parameter(
            description = "ID of the employee to update or create",
            required = true,
            example = "1",
        )
        @PathVariable
        id: Long,
    ): ResponseEntity<Employee> {
        val location =
            ServletUriComponentsBuilder
                .fromCurrentServletMapping()
                .path("/employees/{id}")
                .build(id)
                .toASCIIString()
        val (status, body) =
            repository
                .findById(id)
                .map { employee ->
                    employee.name = newEmployee.name
                    employee.role = newEmployee.role
                    repository.save(employee)
                    HttpStatus.OK to employee
                }.orElseGet {
                    newEmployee.id = id
                    repository.save(newEmployee)
                    HttpStatus.CREATED to newEmployee
                }
        return ResponseEntity.status(status).header("Content-Location", location).body(body)
    }

    @DeleteMapping("/employees/{id}")
    @Operation(
        summary = "Delete an employee",
        description = """
            HTTP Method: DELETE
            
            PROPERTIES:
            - Not Safe: Modifies server state by removing a resource
            - Idempotent: Multiple identical requests produce the same result (resource is deleted)
            
            BEHAVIOR:
            Removes an employee resource from the database. This demonstrates idempotent behavior, 
            calling this endpoint multiple times with the same ID has the same effect. The first 
            call deletes the employee, subsequent calls have no additional effect (employee is 
            already gone).
            
            RESPONSE:
            Returns HTTP 204 (No Content) with no body, indicating successful deletion.
            
            IDEMPOTENCY NOTE:
            Even if the employee doesn't exist, returns 204. This is by design, the desired 
            state (employee not existing) is achieved regardless of whether it existed before.
            
            USE CASE:
            Use this endpoint to permanently remove an employee from the system, for example 
            when an employee leaves the company.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Employee successfully deleted (or didn't exist). No response body.",
            ),
        ],
    )
    fun deleteEmployee(
        @Parameter(
            description = "ID of the employee to delete",
            required = true,
            example = "1",
        )
        @PathVariable
        id: Long,
    ): ResponseEntity<Void> {
        repository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class EmployeeNotFoundException(
    id: Long,
) : Exception("Could not find employee $id")
