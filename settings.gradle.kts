import java.nio.file.Files
import java.nio.file.Path

fun scanProjects(action: (String, String) -> Unit): (Path) -> Unit = { path ->
  val name = path.toFile().name
  val currentPath = path.toFile().path.replace("\\", "/")
  val buildFilePath = "$currentPath/build.gradle"
  if (File(buildFilePath).exists() || File("$buildFilePath.kts").exists()) {
    action(currentPath, name)
  } else {
    Files.list(path)
        .filter { Files.isDirectory(it) }
        .forEach { file ->
          scanProjects(action)(file)
        }
  }
}

val addProjects = scanProjects { currentPath, name ->
  include(name)
  project(":$name").projectDir = File(currentPath)
}

addProjects(file("projects").toPath())

includeBuild("../../mythic/gradle")
includeBuild("../../mythic")
includeBuild("../../imp")

Files.list(file("../../mythic/modules").toPath())
    .forEach { path ->
      includeBuild(path)
    }
