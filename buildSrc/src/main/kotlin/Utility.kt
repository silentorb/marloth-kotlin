import org.gradle.api.Project

var helpers: MutableMap<String, String> = mutableMapOf()

fun getHelper(name: String): String = helpers[name]!!

fun registerHelper(name: String, helperPath: String) {
  helpers.put(name, helperPath)
}

fun requires(project: Project, vararg names: String) {
  names.forEach { project.dependencies.add("compile", project.project(":" + it)) }
}
