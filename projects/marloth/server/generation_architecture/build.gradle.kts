plugins { kotlin("jvm") }

dependencies {
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:sculpting")
}

requires(project, "simulation", "generation_general")
