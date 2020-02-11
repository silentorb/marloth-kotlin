plugins {
  kotlin("jvm")
}

requires(project, "marloth_scenery")

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
  api(group = "org.recast4j", name = "recast", version = "1.2.5")
  api(group = "org.recast4j", name = "detour", version = "1.2.5")
  implementation("silentorb.mythic:aura")
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:sculpting")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:ent")
  implementation("silentorb.mythic:breeze")
  implementation("silentorb.mythic:debugging")
  implementation("silentorb.mythic:happenings")
  implementation("silentorb.mythic:physics")
  implementation("silentorb.mythic:characters")
}
