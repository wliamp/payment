import java.lang.System.getenv

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val tld: String? = getenv("TLD")
val org: String? = getenv("ORG")
val based: String? = getenv("BASED")
val spec: String? = getenv("SPEC")
val repo: String? = getenv("REPO")
val actor: String? = getenv("ACTOR")
val token: String? = getenv("TOKEN")
val id = "payment-authorize-v1-$spec"

group = "$tld.$org.$based"
version = getenv("TAG") ?: ""

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    relocate("package", "$tld.$org.$based.payment.authorize.v1.$spec")
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

        create<MavenPublication>("grpcServer") {
            from(components["java"])
            artifactId = "$id-server"
            pom {
                name.set(artifactId)
                description.set("Reusable gRPC API Servers Specification")
                url.set("https://github.com/$org/$repo")
            }
        }

        create<MavenPublication>("grpcClient") {
            from(components["java"])
            artifactId = "$id-client"
            pom {
                name.set(artifactId)
                description.set("Reusable gRPC API Clients Specification")
                url.set("https://github.com/$org/$repo")
            }
        }

        create<MavenPublication>("restModel") {
            from(components["java"])
            artifactId = "$id-model"
            pom {
                name.set(artifactId)
                description.set("Reusable REST API Models Specification")
                url.set("https://github.com/$org/$repo")
            }
        }

        create<MavenPublication>("restApi") {
            from(components["java"])
            artifactId = "$id-api"
            pom {
                name.set(artifactId)
                description.set("Reusable REST APIs Specification")
                url.set("https://github.com/$org/$repo")
            }
        }

        create<MavenPublication>("restClient") {
            from(components["java"])
            artifactId = "$id-client"
            pom {
                name.set(artifactId)
                description.set("Reusable REST API Clients Specification")
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

tasks.register("event") {
    dependsOn(
        "publishEventPublicationToGitHubPackagesRepository"
    )
}


tasks.register("grpc") {
    dependsOn(
        "publishGrpcServerPublicationToGitHubPackagesRepository",
        "publishGrpcClientPublicationToGitHubPackagesRepository"
    )
}

tasks.register("rest") {
    dependsOn(
        "publishRestModelPublicationToGitHubPackagesRepository",
        "publishRestApiPublicationToGitHubPackagesRepository",
        "publishRestClientPublicationToGitHubPackagesRepository"
    )
}
