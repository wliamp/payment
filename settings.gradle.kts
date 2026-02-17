import java.lang.System.getenv

rootProject.name = "payment"

val moduleDeps = mapOf<String, List<String>>(
    "core" to emptyList(),
)

val included = mutableSetOf<String>()

fun includeRecursive(module: String) =
    module
        .takeIf { included.add(it) }
        ?.also { include(it) }
        ?.let { moduleDeps[it].orEmpty().forEach(::includeRecursive) }

getenv("MODULE")?.takeIf { it.isNotBlank() }?.let {
    includeRecursive(it)
} ?: include(
    "core",
)
