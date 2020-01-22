plugins {
  kotlin("jvm")
}
apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

dependencies {
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.3")
}

requires(project, "randomly", "ent")
