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

fun createYamlMapper(): YAMLMapper {
  val mapper = YAMLMapper()
  mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
  mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
  mapper.registerModule(KotlinModule())
  return mapper
}

fun <T> saveConfig(mapper: YAMLMapper, path: String, config: T) {
  Files.newBufferedWriter(Paths.get(path)).use {
    mapper.writeValue(it, config)
  }
}

fun <T> saveConfig(path: String, config: T) {
  saveConfig(createYamlMapper(), path, config)
}

class ConfigManager<T>(private val path: String, private val config: T) {
  private var previous: String

  init {
    val mapper = createYamlMapper()
    previous = mapper.writeValueAsString(config)
  }

  fun save(){
    val mapper = createYamlMapper()
    val newString = mapper.writeValueAsString(config)
    if (newString != previous) {
      saveConfig(mapper, path, config)
      previous = newString
    }
  }
}