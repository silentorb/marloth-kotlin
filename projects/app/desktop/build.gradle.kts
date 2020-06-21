plugins {
  kotlin("jvm")
  application
}

application {
  applicationName = "marloth"
  mainClass.set("desktop.App")
  applicationDefaultJvmArgs = listOf(
      "-Djava.library.path=\"E:/dev/games/java-freetype/cmake-build-debug/bin\"", // TODO: This isn't the way to include native libraries in production
      "-ea",
      "-Dcom.sun.management.jmxremote"
  )
}

dependencies {
  implementation("silentorb.mythic:mythic-desktop")
}

requires(project, "integration")
