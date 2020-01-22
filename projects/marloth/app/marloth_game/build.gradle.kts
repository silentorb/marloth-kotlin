
apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

requires(project, "marloth_clienting", "configuration", "definition",
    "quartz", "platforming", "generation_architecture", "generation_population",
    "randomly", "simulation", "haft", "persistence"
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
