plugins {
  kotlin("jvm")
}
dependencies {
  implementation("silentorb.mythic:debugging")
  implementation("silentorb.mythic:editing")
}

requires(project, "simulation", "generation_architecture")
