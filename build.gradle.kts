plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.versions.update)
    application
}

group = "com.ck"
version = "1.1"


repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

application {
    mainClass = "MainKt"
}
