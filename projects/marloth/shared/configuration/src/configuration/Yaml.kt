package configuration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

private var globalMapper: YAMLMapper? = null
private var afterburnerModule: AfterburnerModule? = null

fun getAfterburnerModule(): AfterburnerModule {
  if (afterburnerModule == null) {
    afterburnerModule = AfterburnerModule()
    afterburnerModule!!.setUseValueClassLoader(false)
  }
  return afterburnerModule!!
}

fun getObjectMapper(): YAMLMapper {
  if (globalMapper == null) {
    val mapper = YAMLMapper()
    mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
    mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
    mapper.registerModule(KotlinModule())
    mapper.registerModule(getAfterburnerModule())
    globalMapper = mapper
    return mapper
  }

  return globalMapper!!
}

inline fun <reified T> loadConfig(path: String): T? {
  if (File(path).isFile) {
    val mapper = getObjectMapper()
    return Files.newBufferedReader(Paths.get(path)).use {
      mapper.readValue(it, T::class.java)
    }
  }

  return null
}

fun getResourceStream(name: String): InputStream {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResourceAsStream(name)
}

inline fun <reified T> loadYamlResource(path: String): T {
  val content = getResourceStream(path)
  return getObjectMapper().readValue(content, T::class.java)
}

inline fun <reified T> loadYamlResource(path: String, typeref: TypeReference<T>): T {
  val content = getResourceStream(path)
  return getObjectMapper().readValue(content, typeref)
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

  fun save() {
    val mapper = getObjectMapper()
    val newString = mapper.writeValueAsString(config)
    if (newString != previous) {
      saveConfig(mapper, path, config)
      previous = newString
    }
  }
}