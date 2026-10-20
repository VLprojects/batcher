import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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
            jvmToolchain(17)
            explicitApi()
            // Services pull this in with their own Kotlin, from 2.1 up: neither the stdlib nor the
            // metadata may ask for a newer one.
            coreLibrariesVersion = "2.1.0"
            compilerOptions {
                apiVersion = KotlinVersion.KOTLIN_2_1
                languageVersion = KotlinVersion.KOTLIN_2_1
                freeCompilerArgs.add("-Xjsr305=strict")
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
