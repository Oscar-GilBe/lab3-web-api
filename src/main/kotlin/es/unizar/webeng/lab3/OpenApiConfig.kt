package es.unizar.webeng.lab3

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Employee Management REST API")
                    .version("1.0.0")
                    .description( // Detailed description of the API in markdown format
                        """
                        # Employee Management REST API
                        A RESTful API for managing employee records with full CRUD operations.
                        
                        ## RESTful Design Principles
                        This API follows REST architectural constraints:
                        
                        ### HTTP Method Semantics
                        - **GET**: Safe and idempotent - retrieves resources without side effects
                        - **POST**: Unsafe and non-idempotent - creates new resources
                        - **PUT**: Unsafe but idempotent - modifies state but repeated calls have the same effect
                        - **DELETE**: Unsafe but idempotent - removes resources
                        
                        ### Resource-Based URLs
                        - `/employees` - Collection of all employees
                        - `/employees/{id}` - Individual employee resource
                        
                        ### HTTP Status Codes
                        - **200 OK**: Successful PUT update
                        - **201 Created**: Successful POST or PUT creation
                        - **204 No Content**: Successful DELETE
                        - **404 Not Found**: Resource not found
                        - **500 Internal Server Error**: Server error
                        
                        ### Idempotency
                        - **Idempotent operations** (GET, PUT, DELETE): Multiple identical requests have the same effect
                        - **Non-idempotent operations** (POST): Each request creates a new resource
                        
                        ### Safety
                        - **Safe operations** (GET): Read-only, no state modification
                        - **Unsafe operations** (POST, PUT, DELETE): Modify server state
                        
                        ## Features
                        - Create, read, update, and delete employee records
                        - RESTful resource-based URLs
                        - Proper HTTP status codes
                        - Location headers for created resources
                        - JSON request/response format
                        - Comprehensive error handling
                        """.trimIndent(),
                    ).contact(
                        Contact()
                            .name("Oscar-GilBe lab3-web-api repository")
                            .url("https://github.com/Oscar-GilBe/lab3-web-api"),
                    ).license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT"),
                    ),
            ).servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local development server"),
                ),
            )
}
