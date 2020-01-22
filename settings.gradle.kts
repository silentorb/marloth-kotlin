import java.nio.file.Files
import java.nio.file.Path

rootProject.name = "dev_lab"

fun addProjects(path: Path) {
  val name = path.toFile().name
  val currentPath = path.toFile().path.replace("\\", "/")
  val buildFilePath = "$currentPath/build.gradle"
  if (File(buildFilePath).exists() || File("$buildFilePath.kts").exists()) {
    include(name)
    project(":$name").projectDir = path.toFile()
    if (File("$currentPath/helper.kts").exists()) {
//      registerHelper(name, "$currentPath/helper.kts")
    }
  } else {
    Files.list(path)
        .filter { Files.isDirectory(it) }
        .forEach { file ->
          addProjects(file)
        }
  }
}

addProjects(file("projects").toPath())
