plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter")
}

dependencies {
    api(project(":batcher-core"))
    api(project(":batcher-micrometer"))
    api(project(":batcher-spring-boot"))
}
