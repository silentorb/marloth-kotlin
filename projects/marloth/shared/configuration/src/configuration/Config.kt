package configuration

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

inline fun <reified T> loadConfig(path: String): T? {
  if (File(path).isFile()) {
    val mapper = YAMLMapper()
    mapper.registerModule(KotlinModule())

    return Files.newBufferedReader(Paths.get(path)).use {
      mapper.readValue(it, T::class.java)
    }
  }

  return null
}

fun <T> saveConfig(path: String, config: T) {
  val mapper = YAMLMapper()
  mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
  mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
  mapper.registerModule(KotlinModule())

  Files.newBufferedWriter(Paths.get(path)).use {
    mapper.writeValue(it, config)
  }
}