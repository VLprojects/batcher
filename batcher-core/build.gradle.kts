plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter")
}

dependencies {
    api("org.slf4j:slf4j-api:${property("slf4j.version")}")
}
