plugins {
  base
  kotlin("jvm") version Versions.kotlin apply false
}

allprojects {
  group = "marloth"

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }

  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/releases/")
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  dependencies {
    val implementation by configurations
    implementation(kotlin("stdlib-jdk8"))
  }
}
