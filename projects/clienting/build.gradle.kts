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
}

requires(project, "assets", "definition", "scenery", "simulation")
