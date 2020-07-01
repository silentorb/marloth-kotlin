plugins {
  kotlin("jvm")
}

dependencies {
  implementation("silentorb.mythic:haft")
  api("silentorb.mythic:lookinglass")
  implementation("silentorb.mythic:spatial")
  implementation("silentorb.mythic:platforming")
  implementation("silentorb.mythic:bloom")
  implementation("silentorb.mythic:bloom_input")
  implementation("silentorb.mythic:drawing")
  implementation("silentorb.mythic:aura")
  implementation("silentorb.mythic:fathom")

  api("org.lwjgl:lwjgl-glfw:${Versions.lwjgl}")
  implementation("org.lwjgl:lwjgl:${Versions.lwjgl}")
  implementation("org.lwjgl:lwjgl:${Versions.lwjgl}:${Natives.lwjgl}")
  implementation("org.lwjgl:lwjgl-opengl:${Versions.lwjgl}")
  implementation("org.lwjgl:lwjgl-opengl:${Versions.lwjgl}:${Natives.lwjgl}")

  implementation("silentorb.imp:parsing")
  implementation("silentorb.imp:execution")
  implementation("silentorb.imp:libraries_standard")
  implementation("silentorb.mythic:imaging")
  implementation("silentorb.mythic:debugging")
  implementation("silentorb.imp:campaign")
  api("silentorb.mythic:scenery")
}

requires(project, "assets", "definition", "simulation")
