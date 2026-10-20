plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter")
}

dependencies {
    api(project(":batcher-core"))
    // Optional: the Micrometer adapter is wired in only when the service has it.
    compileOnly(project(":batcher-micrometer"))
}
