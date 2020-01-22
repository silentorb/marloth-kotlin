import io.github.cdimascio.dotenv.Dotenv
import org.gradle.internal.os.OperatingSystem

//group "dev-lab"
//version "1.0-SNAPSHOT"

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

val dotEnv = Dotenv.configure().ignoreIfMissing().load()

val _lwjglNatives = when (OperatingSystem.current()) {
  OperatingSystem.WINDOWS -> "natives-windows"
  OperatingSystem.LINUX -> "natives-linux"
  OperatingSystem.MAC_OS -> "natives-macos"
  else -> throw Error("Unsupported Operating System")
}

allprojects {
  apply(plugin = "idea")

  version = "1.0"
  extra["appName"] = "marloth"
  extra["gdxVersion"] = "1.9.9"
  extra["lwjglVersion"] = "3.1.5"
  extra["lwjglNatives"] = _lwjglNatives
  extra["jomlVersion"] = "1.9.8"

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

//  extra["requires"] = {    String... names ->
//    names.each() { project.dependencies.add("compile", project(":" + it)) }
//  }

//  registerHelper("foo", "foo")

//  ext.helpers = [:]
//  ext.registerHelper = { String name, String helperPath ->
//    ext.helpers[name] = helperPath
//  }
}
