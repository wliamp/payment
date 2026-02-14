import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import java.lang.System.getenv

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

fun envOrProp(env: String, prop: String = env, def: String = ""): String =
    getenv(env) ?: (findProperty(prop) as String?) ?: def

val tld = envOrProp("TLD", "kit.algorithm.tld", "kit")
val org = envOrProp("ORG", "kit.algorithm.org", "team")
val tag = envOrProp("TAG", "kit.algorithm.tag", "0.0.1-SNAPSHOT")
val repo = envOrProp("REPO", "kit.algorithm.repo", "repository")
val artId = envOrProp("ARTIFACT_ID", "kit.algorithm.artifact.id", "art")
val artName = envOrProp("ARTIFACT_NAME", "kit.algorithm.artifact.name", "artifact")
val desc = envOrProp("DESC", "kit.algorithm.artifact.desc", "description")
val devId = envOrProp("DEV_ID", "kit.algorithm.dev.id", "dev")
val devName = envOrProp("DEV_NAME", "kit.algorithm.dev.name", "Developer")
val devEmail = envOrProp("DEV_EMAIL", "kit.algorithm.dev.email", "developer@email.dev")
val user = envOrProp("USER", "kit.algorithm.user", "username")
val token = envOrProp("TOKEN", "kit.algorithm.token", "password")

group = "$tld.$org"
version = tag

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    java {
        toolchain {
            languageVersion.set(of(21))
        }
    }

    kotlin {
        jvmToolchain(21)
    }

    group = rootProject.group
    version = rootProject.version

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = artId
                pom {
                    url.set("https://github.com/$org/$repo")
                    name.set(artName)
                    description.set(desc)
                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set(devId)
                            name.set(devName)
                            email.set(devEmail)
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/$org/$repo.git")
                        developerConnection.set("scm:git:ssh://github.com/$org/$repo.git")
                        url.set("https://github.com/$org/$repo")
                    }
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
            snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
            username = user
            password = token
        }
    }
}

tasks.register("listModules") {
    doLast {
        println(
            rootProject
                .subprojects
                .map { it.name }
                .sorted()
                .joinToString("\",\"", "[\"", "\"]")
        )
    }
}
