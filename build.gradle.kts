
buildscript {
  val kotlinVersion = "1.3.61"
  extra["kotlin_version"] = kotlinVersion

  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("io.github.cdimascio:java-dotenv:5.1.3")
  }

}

allprojects {
  apply(plugin = "idea")

  version = "1.0"
  extra["appName"] = "marloth"

  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/releases/")
  }
}
