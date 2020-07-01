plugins {  kotlin("jvm")}

dependencies {
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:debugging")
}

requires(project, "simulation", "definition")
