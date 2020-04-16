plugins {  kotlin("jvm")}

dependencies {
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:randomly")
}

requires(project, "simulation", "definition")
