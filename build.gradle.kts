import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import java.lang.System.getenv

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.0" apply false
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    `maven-publish`
    signing
}

val jvm = 21

fun envOrProp(env: String, prop: String = env, def: String = ""): String =
    getenv(env) ?: (findProperty(prop) as String?) ?: def

val tld = envOrProp("TLD", "kit.payment.tld", "kit")
val org = envOrProp("ORG", "kit.payment.org", "team")
val tag = envOrProp("TAG", "kit.payment.tag", "0.0.1-SNAPSHOT")
val repo = envOrProp("REPO", "kit.payment.repo", "repository")
val art = envOrProp("ART_ID", "kit.payment.artifact.id", "art")
val pomName = envOrProp("POM", "kit.payment.artifact.name", "artifact")
val desc = envOrProp("DESC", "kit.payment.artifact.desc", "description")
val devId = envOrProp("DEV_ID", "kit.payment.dev.id", "dev")
val devName = envOrProp("DEV_NAME", "kit.payment.dev.name", "Developer")
val devEmail = envOrProp("DEV_EMAIL", "kit.payment.dev.email", "developer@email.dev")
val user = envOrProp("USER", "kit.payment.user", "username")
val token = envOrProp("TOKEN", "kit.payment.token", "password")

allprojects {
    group = "$tld.$org"
    version = tag

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
            languageVersion.set(of(jvm))
        }
    }

    kotlin {
        jvmToolchain(jvm)
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.withType<JacocoReport>())
    }

    tasks.withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(true)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = art
                pom {
                    url.set("https://github.com/$org/$repo")
                    name.set(pomName)
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
