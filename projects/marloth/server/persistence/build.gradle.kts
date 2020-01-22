plugins {
  kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

dependencies {
  implementation(group = "org.xerial", name = "sqlite-jdbc", version = "3.25.2")
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
}

requires(project, "simulation")
