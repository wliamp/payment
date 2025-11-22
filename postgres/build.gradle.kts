import java.lang.System.getenv

val host: String? = getenv("HOST")
val post: String? = getenv("POST")
val db: String? = getenv("DATABASE")

subprojects {
    apply(plugin = "org.flywaydb.flyway")

    dependencies {
        implementation("org.flywaydb:flyway-core:11.17.1")
        runtimeOnly("org.flywaydb:flyway-database-postgresql:11.17.1")
    }

    flyway {
        url = "jdbc:postgresql://$host:$post/$db"
        user = getenv("USERNAME") ?: "postgres"
        password = getenv("PASSWORD") ?: "0123456789"
        schemas = arrayOf("public", getenv("SCHEMA") ?: "default")
        locations = arrayOf("classpath:db/migration")
    }
}
