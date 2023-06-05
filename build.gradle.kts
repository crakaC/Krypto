plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "com.crakac"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform("io.kotest:kotest-bom:5.6.2"))
    testImplementation("io.kotest:kotest-assertions-core")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}