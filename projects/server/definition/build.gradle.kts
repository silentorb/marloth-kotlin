plugins {
  kotlin("jvm")
}
dependencies {
  implementation("silentorb.mythic:debugging")
}

requires(project, "simulation", "generation_architecture")
