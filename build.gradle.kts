plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0-Beta3"
    application
}

group = "com.ck"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "MainKt"
}
