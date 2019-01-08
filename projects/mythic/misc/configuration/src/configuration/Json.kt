package configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream

private var globalJsonMapper: ObjectMapper? = null

fun getJsonObjectMapper(): ObjectMapper {
  if (globalJsonMapper == null) {
    val mapper = ObjectMapper()
    val module = KotlinModule()
    mapper.registerModule(module)
    mapper.registerModule(getAfterburnerModule())
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    globalJsonMapper = mapper
  }

  return globalJsonMapper!!
}

inline fun <reified T> loadJsonFile(stream: InputStream): T {
  val result = getJsonObjectMapper().readValue(stream, T::class.java)
  return result
}
