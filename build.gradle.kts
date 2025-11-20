import java.lang.System.getenv

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val tld: String? = getenv("TLD")
val org: String? = getenv("ORG")
val domain: String? = getenv("DOMAIN")
val usecase: String? = getenv("USECASE")
val ver: String? = getenv("VERSION")
val event: String? = getenv("EVENT")
val id: String? = getenv("ID")
val repo: String? = getenv("REPO")
val actor: String? = getenv("ACTOR")
val token: String? = getenv("TOKEN")

group = "$tld.$org.event"
version = getenv("TAG") ?: ""

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    relocate("package", "$tld.$org.event.$domain.$usecase.$ver.$event")
}

publishing {
    publications {
        create<MavenPublication>("event") {
            from(components["java"])
            artifactId = id
            pom {
                name.set(artifactId)
                description.set("Reusable Event Schemas Specification")
                url.set("https://github.com/$org/$repo")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$org/$repo")
            credentials {
                username = actor
                password = token
            }
        }
    }
}
