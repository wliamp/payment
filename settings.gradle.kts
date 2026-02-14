import java.lang.System.getenv

rootProject.name = "payment"

getenv("MODULE")?.takeIf { it.isNotBlank() }?.let {
    include(it)
} ?: include(
    "core",
)
