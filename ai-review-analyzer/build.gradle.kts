import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.jpa") version "1.9.24"
    id("org.asciidoctor.jvm.convert") version "4.0.2"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

extra["testcontainers.version"] = "1.21.4"

val asciidoctorExt: Configuration by configurations.creating
val snippetsDir = file("build/generated-snippets")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.11") // 코틀린 전용 Mock 라이브러리 추가
    testImplementation("com.ninja-squad:springmockk:4.0.2") // 스프링 부트용 @MockkBean 지원
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    outputs.dir(snippetsDir)
}

tasks.withType<AsciidoctorTask> {
    inputs.dir(snippetsDir)
    configurations("asciidoctorExt")
    dependsOn(tasks.withType<Test>())
    doFirst {
        delete(file("src/main/resources/static/docs"))
    }
}

tasks.register<Copy>("copyDocument") {
    dependsOn(tasks.withType<AsciidoctorTask>())
    from(file("build/docs/asciidoc"))
    into(file("src/main/resources/static/docs"))
}

tasks.build {
    dependsOn("copyDocument")
}
