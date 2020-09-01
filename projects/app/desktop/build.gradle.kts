plugins {
  kotlin("jvm")
  application
  id("edu.sc.seis.launch4j") version "2.4.6"
  id("org.beryx.runtime") version "1.11.2"
}

//application {
//  applicationName = "marloth"
//  mainClass.set("desktop.App")
//  setExecutableDir("")
//  applicationDefaultJvmArgs = listOf(
////      "-Djava.library.path=\"E:/dev/games/java-freetype/cmake-build-debug/bin\"", // TODO: This isn't the way to include native libraries in production
//      "-ea"
//  )
//}

dependencies {
  implementation("silentorb.mythic:mythic-desktop")
}

// Build a minimal Java Runtime
runtime {
  options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
  modules.set(listOf(
//      "java.naming", // Included in beryx examples.  Not sure what this is used for.
      "java.xml",  // Needed for Jackson
      "jdk.unsupported", // Needed for Unsafe LWJGL memory functions
      "jdk.zipfs", // Needed for loading enumerated resources from JAR files
      "java.sql" // Used for SQLite
  ))
  jreDir.set(project.layout.buildDirectory.dir("launch4j/jre"))
}

// Build the executable
tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
  outfile = "marloth.exe"
  mainClassName = "desktop.App"
//  icon = "$projectDir/icons/myApp.ico"
  productName = "marloth"
  bundledJre64Bit = true
  jreMinVersion = "14.0.0"
  jreMaxVersion = "14.0.2"
  bundledJrePath = "./jre"
  jreRuntimeBits = "64"
  headerType = "console"
}

requires(project, "integration")
