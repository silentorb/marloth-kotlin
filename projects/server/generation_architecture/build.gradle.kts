plugins { kotlin("jvm") }

dependencies {
  implementation("silentorb.mythic:aura")
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:sculpting")
  implementation("silentorb.mythic:scenery")
  implementation("silentorb.mythic:physics")
  implementation("silentorb.mythic:debugging")
}

requires(project, "simulation", "generation_general")
