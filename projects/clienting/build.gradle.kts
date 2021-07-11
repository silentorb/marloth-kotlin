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

  api("org.lwjgl:lwjgl-glfw:${Versions.lwjgl}")
  implementation("org.lwjgl:lwjgl:${Versions.lwjgl}")
  implementation("org.lwjgl:lwjgl:${Versions.lwjgl}:${Natives.lwjgl}")
  implementation("org.lwjgl:lwjgl-opengl:${Versions.lwjgl}")
  implementation("org.lwjgl:lwjgl-opengl:${Versions.lwjgl}:${Natives.lwjgl}")
  implementation("org.commonmark:commonmark:0.17.2")

  implementation("silentorb.mythic:imaging")
  implementation("silentorb.mythic:debugging")
  implementation("silentorb.imp:campaign")
  implementation("marloth:assets")
  api("silentorb.mythic:scenery")
  api("silentorb.mythic:editing")
  api("silentorb.mythic:editing-lookinglass")
}

requires(project, "definition", "simulation", "marloth_scenery")
