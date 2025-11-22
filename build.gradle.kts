import java.net.URI

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.openapi.generator)
}

group = "es.unizar.webeng"
version = "2025-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.kotlin.reflect)
    implementation(libs.springdoc.openapi.starter.webmvc.ui) // OpenAPI (Swagger) UI dependency
    implementation(libs.spring.boot.starter.validation) // Bean Validation dependency

    runtimeOnly(libs.h2database.h2)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.ninjasquad.springmocck)

    // Dependencies for generated OpenAPI client
    testImplementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

// Configure source sets to include generated client code in tests
kotlin.sourceSets.test {
    kotlin.srcDir("${layout.buildDirectory.get().asFile}/generated-client/src/main/kotlin")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
}

// Disable ktlint for generated code
tasks.named("ktlintTestSourceSetCheck") {
    enabled = false
}
tasks.named("ktlintTestSourceSetFormat") {
    enabled = false
}

// Task to download OpenAPI specification from running application
tasks.register("downloadOpenApiSpec") {
    group = "openapi"
    description = "Download OpenAPI specification from running app"

    val specFile = file("openapi-spec.yaml")
    outputs.file(specFile)

    doLast {
        try {
            println("Downloading spec from http://localhost:8080/api-docs.yaml")
            URI("http://localhost:8080/api-docs.yaml").toURL().openStream().use { input ->
                specFile.outputStream().use { output -> input.copyTo(output) }
            }
            println("Spec saved to: ${specFile.absolutePath}")
        } catch (e: Exception) {
            throw GradleException("Failed! Make sure app is running: .\\gradlew bootRun\nError: ${e.message}")
        }
    }
}

// OpenAPI Generator configuration for generating clients
openApiGenerate {
    generatorName = "kotlin" // Generate Kotlin client
    inputSpec = file("openapi-spec.yaml").toURI().toString() // Path to downloaded spec
    outputDir = "$buildDir/generated-client"
    apiPackage = "es.unizar.webeng.lab3.client.api"
    modelPackage = "es.unizar.webeng.lab3.client.model"
    invokerPackage = "es.unizar.webeng.lab3.client"
    configOptions =
        mapOf(
            "dateLibrary" to "java8",
            "serializationLibrary" to "jackson",
            "library" to "jvm-okhttp4",
        )
}

// Make openApiGenerate depend on downloadOpenApiSpec
tasks.named("openApiGenerate") {
    dependsOn("downloadOpenApiSpec")
}

// Generate client from local spec without downloading
tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateClientFromLocalSpec") {
    group = "openapi"
    description = "Generate Kotlin client from existing OpenAPI spec (no download required)"

    generatorName = "kotlin" // Generate Kotlin client
    inputSpec = file("openapi-spec.yaml").toURI().toString() // Path to downloaded spec
    outputDir = "$buildDir/generated-client"
    apiPackage = "es.unizar.webeng.lab3.client.api"
    modelPackage = "es.unizar.webeng.lab3.client.model"
    invokerPackage = "es.unizar.webeng.lab3.client"
    configOptions =
        mapOf(
            "dateLibrary" to "java8",
            "serializationLibrary" to "jackson",
            "library" to "jvm-okhttp4",
        )
}

// Convenience task
tasks.register("generateClient") {
    group = "openapi"
    description = "Download spec and generate Kotlin client (requires app running)"
    dependsOn("openApiGenerate")
}
