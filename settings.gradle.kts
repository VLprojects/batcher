pluginManagement {
    plugins {
        kotlin("jvm") version extra["kotlin.version"] as String
        id("org.jmailen.kotlinter") version extra["kotlinter.version"] as String
        id("org.gradle.toolchains.foojay-resolver-convention") version extra["foojay.version"] as String
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "batcher"

include("batcher-core")
include("batcher-micrometer")
include("batcher-spring-boot")
include("batcher-spring-boot-starter")
