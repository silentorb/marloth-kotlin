plugins { kotlin("jvm") }

dependencies {
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:drawing")
  implementation("silentorb.mythic:bloom")
  implementation("silentorb.mythic:bloom_input")
  implementation("silentorb.mythic:quartz")
  implementation("silentorb.mythic:platforming")
  implementation("silentorb.mythic:configuration")
  implementation("silentorb.mythic:desktop")
}

requires(project, "generation_architecture", "marloth_clienting", "marloth_game", "simulation")
