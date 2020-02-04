import java.nio.file.Path

rootProject.name = "dev_lab"

apply(from = "projects/mythic/gradle/utility.gradle.kts")

val scanProjects = extra["scanProjects"] as ((String, String) -> Unit) -> (Path) -> Unit

val addProjects = scanProjects { currentPath, name ->
  include(name)
  project(":$name").projectDir = File(currentPath)
}

addProjects(file("projects/marloth").toPath())
addProjects(file("projects/mythic/projects").toPath())

includeBuild("projects/imp") {
  dependencySubstitution {
    substitute(module("silentorb.imp.core:1.0")).with(project(":imp_core"))
    substitute(module("silentorb.imp.execution:1.0")).with(project(":imp_execution"))
    substitute(module("silentorb.imp.parsing:1.0")).with(project(":imp_parsing"))
    substitute(module("silentorb.imp.libraries.standard:1.0")).with(project(":imp_libraries_standard"))
    substitute(module("silentorb.imp.libraries.standard.implementation:1.0")).with(project(":imp_libraries_standard_implementation"))
  }
}

includeBuild("projects/mythic/gradle") {
  dependencySubstitution {
    substitute(module("mythic.gradle.assets.general:1.0")).with(project(":assets_general"))
    substitute(module("mythic.gradle.assets.svg:1.0")).with(project(":assets_svg"))
  }
}
