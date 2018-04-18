package rendering

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mythic.glowing.SimpleTriangleMesh
import mythic.spatial.Vector4
import org.lwjgl.BufferUtils
import java.io.DataInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

enum class AccessorType {
  SCALAR,
  VEC3
}

enum class AttributeType {
  NORMAL,
  POSITION
}

val attributeMap = mapOf(
    AttributeType.NORMAL to AttributeName.normal,
    AttributeType.POSITION to AttributeName.position
)

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

data class BufferInfo(
    var byteLength: Int
)

data class Primitive(
    var attributes: Map<AttributeType, Int>,
    var indices: Int,
    var material: Int
)

data class MeshInfo(
    var primitives: List<Primitive>
)

data class Metallic(
    var baseColorFactor: List<Float>,
    var metallicFactor: Float
)

data class MaterialInfo(
    var pbrMetallicRoughness: Metallic
)

data class GltfInfo(
    var accessors: List<Accessor>,
    var bufferViews: List<BufferView>,
    var buffers: List<BufferInfo>,
    var meshes: List<MeshInfo>,
    var materials: List<MaterialInfo>
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

fun readFloat(buffer: ByteBuffer) =
    java.lang.Float.intBitsToFloat(Integer.reverseBytes(buffer.getInt()))

fun loadGltfByteBuffer(name: String, info: GltfInfo): ByteBuffer {
  val inputStream = loadBinaryResource(name + ".bin")
  val dataStream = DataInputStream(inputStream)
  val buffer = BufferUtils.createByteBuffer(info.buffers[0].byteLength)
  dataStream.use {
    while (dataStream.available() > 0) {
      buffer.put(dataStream.readByte())
    }
  }

  return buffer
}

fun arrayToVector4(value: List<Float>) = Vector4(value[0], value[1], value[2], value[3])

fun loadGltf(vertexSchemas: VertexSchemas, name: String): ModelElements {
  val info = loadJsonResource<GltfInfo>(name + ".gltf")
  val buffer = loadGltfByteBuffer(name, info)
  val vertexSchema = vertexSchemas.imported

  val result = info.meshes[0].primitives.map { primitive ->

    val triangleCount = info.accessors[primitive.indices].count / 3
    val vertexCount = info.accessors[primitive.attributes[AttributeType.POSITION]!!].count

    val indexCount = triangleCount * 3
    val indices = BufferUtils.createIntBuffer(indexCount)
    val vertices = BufferUtils.createFloatBuffer(3 * 2 * vertexCount)

    buffer.position(info.bufferViews[primitive.indices].byteOffset)
    for (i in 0 until triangleCount) {
      for (x in 0 until 3) {
        val value = buffer.get().toInt()
//        println(value)
        indices.put(value)
      }
    }

    for (attribute in primitive.attributes) {
      val bufferView = info.bufferViews[attribute.value]
      buffer.position(bufferView.byteOffset)
      val vertexAttribute = vertexSchema.getAttribute(attributeMap[attribute.key]!!)
      vertices.position()
      for (i in 0 until vertexCount) {
        vertices.position(vertexAttribute.offset + i * vertexSchema.floatSize)
        for (x in 0 until 3) {
          val value = buffer.getFloat()
          println(value)
          vertices.put(value)
        }
      }
    }

    vertices.flip()
    indices.flip()

    val materialSource = info.materials[primitive.material]
    val color = arrayToVector4(materialSource.pbrMetallicRoughness.baseColorFactor)

    ModelElement(
        mesh = SimpleTriangleMesh(vertexSchema, vertices, indices),
        material = Material(
            color = color
        )
    )
  }

  return result
}