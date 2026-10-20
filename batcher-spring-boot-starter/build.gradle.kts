// Dependencies only, like the Spring Boot starters themselves.
plugins {
    `java-library`
}

dependencies {
    api(project(":batcher-core"))
    api(project(":batcher-micrometer"))
    api(project(":batcher-spring-boot"))
}
