package rendering

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mythic.glowing.SimpleTriangleMesh
import org.lwjgl.BufferUtils
import java.io.DataInputStream
import java.io.InputStream
import java.util.*

enum class AccessorType {
  SCALAR,
  VEC3
}

data class Accessor(
    var bufferView: Int,
    var componentType: Int,
    var count: Int,
    var type: AccessorType
)

data class BufferView(
    var buffer: Int,
    var byteLength: Int,
    var byteOffset: Int,
    var target: Int
)

data class GltfInfo(
    var accessors: List<Accessor>,
    var bufferViews: List<BufferView>
)

private fun loadBinaryResource(name: String): InputStream {
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

inline fun <reified T> loadJsonResource(path: String): T {
  val mapper = ObjectMapper()
  mapper.registerModule(KotlinModule())
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  val content = loadTextResource(path)
  return mapper.readValue(content, T::class.java)
}

fun readInt(dataStream: DataInputStream) = Integer.reverseBytes(dataStream.readInt())// and 0xFFFFFFFFL.toInt()
fun readFloat(dataStream: DataInputStream) =
    java.lang.Float.intBitsToFloat(Integer.reverseBytes(dataStream.readInt()))

fun loadGltf(vertexSchemas: VertexSchemas, name: String): ModelElements {
  val info = loadJsonResource<GltfInfo>(name + ".gltf")
  val inputStream = loadBinaryResource(name + ".bin")
  val dataStream = DataInputStream(inputStream)

  val triangleCount = info.accessors[0].count / 3
  val vertexCount = info.accessors[1].count

//  val triangleCount = 6
//  val vertexCount = 12
  val indexCount = triangleCount * 3
  val indices = BufferUtils.createIntBuffer(indexCount)
  val vertices = BufferUtils.createFloatBuffer(3 * 2 * vertexCount)

  dataStream.use {
    for (i in 0 until triangleCount) {
      for (x in 0 until 3) {
        val value = dataStream.readByte().toInt()
//        println(value)
        indices.put(value)
      }
    }

    val padding = info.bufferViews[1].byteOffset - info.bufferViews[0].byteLength
    for (i in 0 until padding) {
      dataStream.readByte()
    }

    for (i in 0 until vertexCount) {
      for (x in 0 until 3) {
        val value = readFloat(dataStream)
//        val value = dataStream.readFloat()
        println(value)
        vertices.put(value)
      }
      vertices.position(vertices.position() + 3)
    }

    vertices.flip()

    for (i in 0 until vertexCount) {
      vertices.position(vertices.position() + 3)
      for (x in 0 until 3) {
        val value = readFloat(dataStream)
//        val value = dataStream.readFloat()
        println(value)
        vertices.put(value)
      }
    }
  }

  vertices.flip()
  indices.flip()

  return listOf(
      ModelElement(
          mesh = SimpleTriangleMesh(vertexSchemas.imported, vertices, indices),
          material = Material()
      )
  )
}