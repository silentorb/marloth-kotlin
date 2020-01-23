
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

//  if (project.convention.findPlugin(JavaPluginConvention)) {
//    sourceSets.main.kotlin.outDir = new File (buildDir, "classes/main/kotlin")
//    sourceCompatibility = 1.8
//    targetCompatibility = 1.8
//  }

  fun projectRequires(vararg names: String) {
    names.forEach { project.dependencies.add("compile", project.project(":" + it)) }
  }
  extra["requires"] = ::projectRequires

}
