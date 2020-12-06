
dependencies {
  implementation("silentorb.imp:execution")
  implementation("silentorb.imp:libraries_standard")
  implementation("silentorb.imp:campaign")
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:scenery")
  implementation("silentorb.mythic:randomly")
  implementation("silentorb.mythic:debugging")
  api("silentorb.mythic:fathom")
}

requires(project, "simulation")
