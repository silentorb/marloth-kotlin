plugins {
  kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

requires(project,"aura", "spatial", "sculpting", "randomly", "marloth_scenery", "ent", "breeze", "debugging",
    "happenings", "physics", "characters"
)

buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
  }
}

apply(plugin = "org.junit.platform.gradle.plugin")

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
  api(group = "org.recast4j", name = "recast", version = "1.2.5")
  api(group = "org.recast4j", name = "detour", version = "1.2.5")
}
