plugins {
  kotlin("jvm")
}

requires(project, "scenery")

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
  api(group = "org.recast4j", name = "recast", version = "1.2.5")
  api(group = "org.recast4j", name = "detour", version = "1.2.5")
  api("silentorb.mythic:aura")
  api("silentorb.mythic:spatial")
  api("silentorb.mythic:sculpting")
  api("silentorb.mythic:randomly")
  api("silentorb.mythic:ent")
  api("silentorb.mythic:breeze")
  api("silentorb.mythic:debugging")
  api("silentorb.mythic:happenings")
  api("silentorb.mythic:physics")
  api("silentorb.mythic:characters")
}
