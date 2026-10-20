plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter")
}

dependencies {
    api(project(":batcher-core"))
    api("io.micrometer:micrometer-core:${property("micrometer.version")}")
}
