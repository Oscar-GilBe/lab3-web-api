package es.unizar.webeng.lab3

import es.unizar.webeng.lab3.client.api.EmployeeManagementApi
import es.unizar.webeng.lab3.client.model.Employee
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.openapitools.client.infrastructure.ClientException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

/**
 * Test suite using the OpenAPI generated client
 *
 * Demonstrates complete CRUD operations and error handling using
 * the auto-generated API client instead of manual HTTP calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GeneratedClientExampleTest {
    @LocalServerPort
    private var port: Int = 0

    @Nested
    @DisplayName("Complete CRUD workflow")
    inner class CrudWorkflow {
        @Test
        fun `should perform complete CRUD lifecycle`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")

            // CREATE: Post a new employee
            val newEmployee =
                Employee(
                    id = null,
                    name = "John Doe",
                    role = "Software Engineer",
                )

            val createdEmployee = api.newEmployee(newEmployee)
            println("Empleado creado: $createdEmployee")
            assertNotNull(createdEmployee.id, "El empleado creado debe tener un ID")
            assertEquals("John Doe", createdEmployee.name)
            assertEquals("Software Engineer", createdEmployee.role)

            // READ ALL: Get all employees
            val allEmployees = api.all()
            println("Total de empleados: ${allEmployees.size}")
            assertTrue(allEmployees.isNotEmpty(), "Debe haber al menos un empleado")
            assertTrue(allEmployees.any { it.id == createdEmployee.id }, "Debe incluir el empleado creado")

            // READ ONE: Get specific employee by ID
            val employeeId = createdEmployee.id!!
            val retrievedEmployee = api.one(employeeId)
            println("Empleado obtenido: $retrievedEmployee")
            assertEquals(createdEmployee.id, retrievedEmployee.id)
            assertEquals(createdEmployee.name, retrievedEmployee.name)
            assertEquals(createdEmployee.role, retrievedEmployee.role)

            // UPDATE: Update existing employee
            val updatedEmployee =
                Employee(
                    id = employeeId,
                    name = "John Snow",
                    role = "Senior Software Engineer",
                )
            val result = api.replaceEmployee(employeeId, updatedEmployee)
            println("Empleado actualizado: $result")
            assertEquals("Senior Software Engineer", result.role)
            assertEquals("John Snow", result.name)
            assertEquals(employeeId, result.id)

            // Verify update persisted
            val verifyUpdate = api.one(employeeId)
            assertEquals("Senior Software Engineer", verifyUpdate.role)
            assertEquals("John Snow", verifyUpdate.name)
            assertEquals(employeeId, verifyUpdate.id)

            // DELETE: Remove employee
            api.deleteEmployee(employeeId)
            println("Empleado eliminado correctamente")

            // Verify deletion: should throw 404
            val exception =
                assertThrows(ClientException::class.java) {
                    api.one(employeeId)
                }
            assertEquals(404, exception.statusCode)
        }
    }

    @Nested
    @DisplayName("Error handling scenarios")
    inner class ErrorHandling {
        @Test
        fun `should return 404 when getting non-existent employee`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")
            val nonExistentId = 999999L

            val exception =
                assertThrows(ClientException::class.java) {
                    api.one(nonExistentId)
                }

            assertEquals(404, exception.statusCode)
            println("Error esperado 404 al buscar empleado inexistente: ${exception.message}")
        }

        @Test
        fun `should return 404 when updating non-existent employee`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")
            val nonExistentId = 999999L

            val updateData =
                Employee(
                    id = nonExistentId,
                    name = "Ghost Employee",
                    role = "Non-existent",
                )

            val exception =
                assertThrows(ClientException::class.java) {
                    api.replaceEmployee(nonExistentId, updateData)
                }

            assertEquals(404, exception.statusCode)
            println("Error esperado 404 al intentar PUT en empleado inexistente: ${exception.message}")
        }
    }

    @Nested
    @DisplayName("Idempotency tests")
    inner class IdempotencyTests {
        @Test
        fun `PUT should be idempotent, same update twice yields same result`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")

            // Create employee
            val newEmployee =
                Employee(
                    id = null,
                    name = "Jane Smith",
                    role = "Developer",
                )
            val created = api.newEmployee(newEmployee)
            val employeeId = created.id!!

            // First update
            val updateData =
                Employee(
                    id = employeeId,
                    name = "Jane Simpson",
                    role = "Senior Developer",
                )
            val firstUpdate = api.replaceEmployee(employeeId, updateData)
            assertEquals("Senior Developer", firstUpdate.role)
            assertEquals("Jane Simpson", firstUpdate.name)
            assertEquals(employeeId, firstUpdate.id)

            // Second identical update
            val secondUpdate = api.replaceEmployee(employeeId, updateData)
            assertEquals("Senior Developer", secondUpdate.role)
            assertEquals("Jane Simpson", secondUpdate.name)
            assertEquals(employeeId, secondUpdate.id)

            assertEquals(firstUpdate.id, secondUpdate.id)
            assertEquals(firstUpdate.name, secondUpdate.name)
            assertEquals(firstUpdate.role, secondUpdate.role)

            println("PUT idempotente verificado: mismo resultado en ambas llamadas")

            // Cleanup
            api.deleteEmployee(employeeId)
        }

        @Test
        fun `GET should be safe and idempotent, multiple calls return same result`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")

            // Create employee
            val newEmployee =
                Employee(
                    id = null,
                    name = "Safe Test",
                    role = "Tester",
                )
            val created = api.newEmployee(newEmployee)
            val employeeId = created.id!!

            // Multiple GET calls
            val first = api.one(employeeId)
            val second = api.one(employeeId)
            val third = api.one(employeeId)

            assertEquals(first.id, second.id)
            assertEquals(second.id, third.id)
            assertEquals(first.name, second.name)
            assertEquals(second.name, third.name)
            assertEquals(first.role, second.role)
            assertEquals(second.role, third.role)

            assertEquals(employeeId, first.id)
            assertEquals("Safe Test", first.name)
            assertEquals("Tester", first.role)

            println("GET seguro e idempotente verificado")

            // Cleanup
            api.deleteEmployee(employeeId)
        }

        @Test
        fun `delete should be idempotent, multiple deletes return 204`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")

            // Create employee
            val newEmployee =
                Employee(
                    id = null,
                    name = "To Delete",
                    role = "Temporary",
                )
            val created = api.newEmployee(newEmployee)
            val employeeId = created.id!!

            // First delete
            api.deleteEmployee(employeeId)
            println("Primera eliminación exitosa")

            // Second delete, should also succeed (idempotent)
            api.deleteEmployee(employeeId)
            println("Segunda eliminación exitosa (idempotencia)")

            // Verify employee doesn't exist
            val exception =
                assertThrows(ClientException::class.java) {
                    api.one(employeeId)
                }
            assertEquals(404, exception.statusCode)
        }
    }

    @Nested
    @DisplayName("POST non-idempotency test")
    inner class PostNonIdempotency {
        @Test
        fun `POST should not be idempotent, creates new resource each time`() {
            val api = EmployeeManagementApi(basePath = "http://localhost:$port")

            val employeeData =
                Employee(
                    id = null,
                    name = "Duplicate Test",
                    role = "Analyst",
                )

            // First POST
            val first = api.newEmployee(employeeData)
            assertNotNull(first.id)
            assertEquals("Duplicate Test", first.name)
            assertEquals("Analyst", first.role)

            // Second POST with same data
            val second = api.newEmployee(employeeData)
            assertNotNull(second.id)
            assertEquals("Duplicate Test", second.name)
            assertEquals("Analyst", second.role)

            // IDs should be different (not idempotent)
            assertTrue(first.id != second.id, "POST debe crear recursos diferentes con IDs únicos")
            println("POST no idempotente verificado: ID1=${first.id}, ID2=${second.id}")

            // Cleanup
            api.deleteEmployee(first.id!!)
            api.deleteEmployee(second.id!!)
        }
    }
}
