plugins {
  kotlin("jvm")
  id("org.beryx.runtime") version "1.11.2"
  id("edu.sc.seis.launch4j") version "2.4.6"
}

dependencies {
  implementation("silentorb.mythic:mythic-desktop")
  implementation("silentorb.mythic:mythic-logging")
  implementation("org.tinylog:tinylog-api:2.3.2")
  runtimeOnly("org.tinylog:tinylog-impl:2.3.2")
  runtimeOnly("org.tinylog:slf4j-tinylog:2.3.2")
}

requires(project, "integration")

// Build a minimal Java Runtime
runtime {
  options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
  modules.set(listOf(
      "java.xml", // Needed for Jackson
      "jdk.unsupported", // Needed for Unsafe LWJGL memory functions
      "jdk.zipfs", // Needed for loading enumerated resources from JAR files
      "java.sql", // Needed for SQLite
      "java.management" // Needed for Tinylog
  ))
  jreDir.set(rootDir.resolve("out/dist/marloth-${project.properties["version"]}/jre"))
}

tasks.register<Copy>("compileDist") {
  from(buildDir.resolve("launch4j"))
  into(rootDir.resolve("out/dist/marloth-${project.properties["version"]}"))
  dependsOn("copyFonts", "copyManual", "runtime", "createAllExecutables")
}

// Build the executable
tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
  outfile = "marloth.exe"
  mainClassName = "desktop.App"
  icon = "$projectDir/icons/m.ico"
  productName = "Marloth"
  fileDescription = "Marloth"
  bundledJre64Bit = true
  jreMinVersion = "14.0.0"
  jreMaxVersion = "14.0.2"
  bundledJrePath = "./jre"
  jreRuntimeBits = "64"
  jvmOptions = setOf("-XX:MaxGCPauseMillis=8")
  headerType = "gui"
  copyright = "©Silent Orb"
}

tasks.register<Copy>("copyFonts") {
  from("fonts")
  include("*.ttf")
  into(rootDir.resolve("out/dist/marloth-${project.properties["version"]}/fonts"))
}

tasks.register<Copy>("copyManual") {
  from(project.rootDir.resolveSibling("marloth-assets/assets/src/main/resources/docs/manual.md"))
  into(rootDir.resolve("out/dist/marloth-${project.properties["version"]}"))
  rename { "README.md" }
}

tasks.register<Zip>("zipDist") {
  archiveFileName.set("marloth-${project.properties["version"]}.zip")
  destinationDirectory.set(rootDir.resolve("out/dist"))
  from(rootDir.resolve("out/dist/marloth-${project.properties["version"]}"))
  dependsOn("createAllExecutables")
}
