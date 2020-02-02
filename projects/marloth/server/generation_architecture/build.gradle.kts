apply(from = "${rootProject.projectDir}/build_kotlin.gradle")
requires(project, "spatial", "randomly", "sculpting", "simulation", "generation_general")

// Specifications

buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
  }
}

apply(plugin = "org.junit.platform.gradle.plugin")