import java.nio.file.Files
import java.nio.file.Path

rootProject.name = "dev_lab"

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

addProjects(file("projects/marloth").toPath())

includeBuild("../../mythic/gradle") {
  dependencySubstitution {
    substitute(module("mythic.gradle.assets.general:1.0")).with(project(":assets_general"))
    substitute(module("mythic.gradle.assets.svg:1.0")).with(project(":assets_svg"))
  }
}

includeBuild("../../mythic")
