import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    kotlin("jvm") apply false
    id("org.jmailen.kotlinter") apply false
}

subprojects {
    group = property("group")!!
    version = property("version")!!

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
            jvmToolchain(25)
            explicitApi()
            compilerOptions {
                allWarningsAsErrors = true
                // `fun onBatchWritten(...) {}` in an interface compiles to a JVM default method only. Without
                // this it is also copied to BatcherMeter$DefaultImpls for Kotlin before 2.2, and that class
                // would stay in the published API for good.
                jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
            }
            // A breaking change shows up as a diff of the checked-in dump.
            @OptIn(ExperimentalAbiValidation::class)
            abiValidation()
        }

        dependencies {
            "testImplementation"(platform("org.junit:junit-bom:${property("junit.version")}"))
            "testImplementation"("org.junit.jupiter:junit-jupiter")
            "testImplementation"("org.awaitility:awaitility:${property("awaitility.version")}")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
