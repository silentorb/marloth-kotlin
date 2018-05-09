package rendering.meshes.loading

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mythic.spatial.Vector4
import java.io.IOException
import java.io.InputStream
import java.util.*

fun loadBinaryResource(name: String): InputStream {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResourceAsStream(name)
}

fun loadTextResource(name: String): String {
  val classloader = Thread.currentThread().contextClassLoader
  val inputStream = classloader.getResourceAsStream(name)
  val s = Scanner(inputStream).useDelimiter("\\A")
  val result = if (s.hasNext()) s.next() else ""
  return result
}

fun parseFloat(node: JsonNode): Float =
    (node.numberValue() as Double).toFloat()

class Vector4Deserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<Vector4>(vc) {

  @Throws(IOException::class, JsonProcessingException::class)
  override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Vector4 {
    val node = jp.getCodec().readTree<ArrayNode>(jp)
    val result = Vector4(
        parseFloat(node.get(0)),
        parseFloat(node.get(1)),
        parseFloat(node.get(2)),
        parseFloat(node.get(3))
    )
    return result
  }
}

inline fun <reified T> loadJsonResource(path: String): T {
  val mapper = ObjectMapper()
  val module = KotlinModule()
  module.addDeserializer(Vector4::class.java, Vector4Deserializer(null))
  mapper.registerModule(module)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  val content = loadTextResource(path)
  return mapper.readValue(content, T::class.java)
}
