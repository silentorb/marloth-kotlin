plugins {
  kotlin("jvm")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
  api(group = "org.recast4j", name = "recast", version = "1.2.5")
  api(group = "org.recast4j", name = "detour", version = "1.2.5")
  api(group = "org.recast4j", name = "detour-crowd", version = "1.2.5")
  api("silentorb.mythic:aura")
  api("silentorb.mythic:spatial")
  api("silentorb.mythic:randomly")
  api("silentorb.mythic:ent")
  api("silentorb.mythic:ent-scenery")
  api("silentorb.mythic:breeze")
  api("silentorb.mythic:happenings")
  api("silentorb.mythic:physics")
  api("silentorb.mythic:scenery")
  implementation("silentorb.mythic:debugging")
  implementation("silentorb.mythic:mythic-shape-meshes")
  api("silentorb.mythic:cameraman")
}

requires(project, "marloth_scenery")
