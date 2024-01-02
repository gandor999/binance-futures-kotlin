plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "org.gandor999"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.github.binance:binance-futures-connector-java:3.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}