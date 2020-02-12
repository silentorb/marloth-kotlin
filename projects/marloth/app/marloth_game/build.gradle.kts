plugins { kotlin("jvm") }

dependencies {
  implementation("silentorb.mythic:bloom")
  implementation("silentorb.mythic:bloom_input")
  implementation("silentorb.mythic:configuration")
  implementation("silentorb.mythic:quartz")
  implementation("silentorb.mythic:platforming")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:haft")
}

requires(project, "marloth_clienting", "definition", "generation_architecture", "generation_population",
    "simulation", "persistence"
)
