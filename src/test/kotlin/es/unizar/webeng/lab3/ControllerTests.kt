package es.unizar.webeng.lab3

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.Optional

private val MANAGER_REQUEST_BODY = { name: String ->
    """
    { 
        "role": "Manager", 
        "name": "$name" 
    }
    """
}

private val MANAGER_RESPONSE_BODY = { name: String, id: Int ->
    """
    { 
       "name" : "$name",
       "role" : "Manager",
       "id" : $id
    }
    """
}

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ControllerTests {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var employeeRepository: EmployeeRepository

    @Test
    fun `POST is not safe and not idempotent`() {
        // POST is not idempotent - each call creates a new resource.

        // Mock setup for POST test
        every {
            employeeRepository.save(any<Employee>())
        } answers {
            Employee("Mary", "Manager", 1)
        } andThenAnswer {
            Employee("Mary", "Manager", 2)
        }

        mvc
            .post("/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Mary")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Mary", 1))
                }
            }

        mvc
            .post("/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Mary")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Location", "http://localhost/employees/2") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Mary", 2))
                }
            }

        // Verification for POST test
        verify(exactly = 2) {
            employeeRepository.save(any<Employee>())
        }

        // These methods should NOT be called
        verify(exactly = 0) {
            employeeRepository.findById(any())
            employeeRepository.deleteById(any())
            employeeRepository.findAll()
        }
    }

    @Test
    fun `GET is safe and idempotent`() {
        // GET is safe and idempotent - it only reads data without side effects.

        // Mock setup for GET test
        // Successful retrieval case
        every {
            employeeRepository.findById(1)
        } answers {
            Optional.of(Employee("Mary", "Manager", 1))
        }

        // Unsuccessful retrieval cases
        every {
            employeeRepository.findById(2)
        } answers {
            Optional.empty()
        }

        mvc.get("/employees/1").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_RESPONSE_BODY("Mary", 1))
            }
        }

        mvc.get("/employees/1").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_RESPONSE_BODY("Mary", 1))
            }
        }

        mvc.get("/employees/2").andExpect {
            status { isNotFound() }
        }

        // Verification for GET test
        verify(exactly = 2) {
            employeeRepository.findById(1)
        }

        verify(exactly = 1) {
            employeeRepository.findById(2)
        }

        // These methods should NOT be called
        verify(exactly = 0) {
            employeeRepository.save(any<Employee>())
            employeeRepository.deleteById(any())
            employeeRepository.findAll()
        }
    }

    @Test
    fun `PUT is idempotent but not safe`() {
        // PUT is idempotent but not safe - it modifies state but repeated calls have the same effect.

        // Mock setup for PUT test
        // First call simulates resource creation, second call simulates update
        every {
            employeeRepository.findById(1)
        } answers {
            Optional.empty()
        } andThenAnswer {
            Optional.of(Employee("Tom", "Manager", 1))
        }

        // Simulate saving the employee
        every {
            employeeRepository.save(any<Employee>())
        } answers {
            Employee("Tom", "Manager", 1)
        }

        mvc
            .put("/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Tom")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Content-Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Tom", 1))
                }
            }

        mvc
            .put("/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Tom")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                header { string("Content-Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Tom", 1))
                }
            }

        // Complete the verification for PUT test"
        verify(exactly = 2) {
            employeeRepository.findById(1)
            employeeRepository.save(any<Employee>())
        }

        // These methods should NOT be called
        verify(exactly = 0) {
            employeeRepository.findAll()
            employeeRepository.deleteById(any())
        }
    }

    @Test
    fun `DELETE is idempotent but not safe`() {
        // DELETE is idempotent but not safe - it modifies state but repeated calls have the same effect.

        // Mock setup for DELETE test
        // DELETE does not call findById() in the controller
        // First call simulates existing resource, second call simulates already deleted resource
        every {
            employeeRepository.findById(1)
        } answers {
            Optional.of(Employee("Tom", "Manager", 1))
        } andThenAnswer {
            Optional.empty()
        }

        // justRun allows void methods to be called without specifying return values
        justRun {
            employeeRepository.deleteById(1)
        }

        mvc.delete("/employees/1").andExpect {
            status { isNoContent() }
        }

        mvc.delete("/employees/1").andExpect {
            status { isNoContent() }
        }

        // Verification for DELETE test
        verify(exactly = 2) {
            employeeRepository.deleteById(1)
        }

        // These methods should NOT be called
        verify(exactly = 0) {
            employeeRepository.save(any<Employee>())
            employeeRepository.findById(any())
            employeeRepository.findAll()
        }
    }
}
