import io.github.cdimascio.dotenv.Dotenv
import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.Paths

fun requires(project: Project, vararg names: String) {
  names.forEach { project.dependencies.add("compile", project.project(":" + it)) }
}

private fun findDotEnvFile(path: Path = Paths.get(System.getProperty("user.dir"))): String? {
  if (Paths.get(path.toString(), ".env").toFile().exists()) {
    return path.toString()
  }
  if (path.parent != null)
    return findDotEnvFile(path.parent)

  return null
}

val dotEnv = Dotenv
    .configure()
    .directory(findDotEnvFile() ?: "./")
    .ignoreIfMissing()
    .load()

fun getOptionalConfigValue(name: String): String? =
    dotEnv.get(name)

fun getRequiredConfigValue(name: String): String {
  val value = dotEnv.get(name)
  return value ?: throw Error("Environment variable $name must be set. (Working dir=${System.getProperty("user.dir")}")
}
