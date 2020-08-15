plugins { kotlin("jvm") }

dependencies {
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:scenery")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:sculpting")
  implementation("silentorb.mythic:debugging")
}

requires(project, "simulation")
