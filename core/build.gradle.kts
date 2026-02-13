import org.gradle.api.tasks.testing.Test

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
