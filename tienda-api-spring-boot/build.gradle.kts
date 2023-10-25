plugins {
    java // Plugin de Java
    id("org.springframework.boot") version "3.1.4" // Versión de Spring Boot
    id("io.spring.dependency-management") version "1.1.3" // Gestión de dependencias
    id("jacoco") // Plugin de Jacoco para test de cobertura
}

group = "dev.joseluisgs"
version = "0.0.1-SNAPSHOT"

java {
    // versión de Java
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    // Dependencias de Spring Web for HTML Apps y Rest
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Spring Data JPA par SQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Spring Data JPA para MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    // Validación
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // H2 Database
    runtimeOnly("com.h2database:h2")

    // Para usar con jackson el controlador las fechas: LocalDate, LocalDateTime, etc
    // Lo podemos usar en el test o en el controlador, si hiciese falta, por eso está aquí
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Para pasar a XML los responses, negocacion de contenido
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")


    // Dependencias para Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // MongoDB para test, pero no es necesario, usamos sus repositorios
    // testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring31x:4.9.3")

}

tasks.withType<Test> {
    useJUnitPlatform() // Usamos JUnit 5
    // finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.test {

}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}
