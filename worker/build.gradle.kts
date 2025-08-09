plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.jitinsharma.worker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        nodejs()
        binaries.executable()
        compilations.all {
            compileTaskProvider {
                compilerOptions.freeCompilerArgs.add("-Xir-generate-inline-anonymous-functions")
            }
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonMain.dependencies {
            implementation(projects.shared)
        }
        jsTest.dependencies {
            implementation(libs.kotlin.test)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.named("jsTest") {
    outputs.upToDateWhen { false }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest> {
    testLogging {
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
