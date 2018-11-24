package configuration

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private var globalMapper: YAMLMapper? = null

fun getObjectMapper(): YAMLMapper {
  if (globalMapper == null) {
    val mapper = YAMLMapper()
    mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
    mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
    mapper.registerModule(KotlinModule())
    mapper.registerModule(AfterburnerModule())
    globalMapper = mapper
    return mapper
  }

  return globalMapper!!
}

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

fun <T> saveConfig(mapper: YAMLMapper, path: String, config: T) {
  Files.newBufferedWriter(Paths.get(path)).use {
    mapper.writeValue(it, config)
  }
}

fun <T> saveConfig(path: String, config: T) {
  saveConfig(getObjectMapper(), path, config)
}

class ConfigManager<T>(private val path: String, private val config: T) {
  private var previous: String

  init {
    val mapper = getObjectMapper()
    previous = mapper.writeValueAsString(config)
  }

  fun save(){
    val mapper = getObjectMapper()
    val newString = mapper.writeValueAsString(config)
    if (newString != previous) {
      saveConfig(mapper, path, config)
      previous = newString
    }
  }
}