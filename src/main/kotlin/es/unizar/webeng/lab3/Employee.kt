package es.unizar.webeng.lab3

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
@Schema(description = "Employee entity representing a person working in the organization")
data class Employee(
    @Schema(
        description = "Full name of the employee",
        example = "John Doe",
        required = true,
    )
    var name: String,

    @Schema(
        description = "Job role or position of the employee",
        example = "Manager",
        required = true,
    )
    var role: String,

    @Schema(
        description = "Unique identifier for the employee. Auto-generated when creating via POST.",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY, // id is read-only because it's auto-generated
    )
    @Id
    @GeneratedValue
    var id: Long? = null,
)
