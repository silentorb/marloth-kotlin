plugins {
  kotlin("jvm")
}
plugins {  kotlin("jvm")}

dependencies {
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.3")
}

requires(project, "randomly", "ent")
