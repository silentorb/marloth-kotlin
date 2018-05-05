package rendering.meshes

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
import com.fasterxml.jackson.annotation.JsonCreator
import rendering.Material


enum class AccessorType {
  SCALAR,
  MAT4,
  VEC2,
  VEC3,
  VEC4
}

enum class ComponentType(val value: Int) {
  Byte(5120),
  UnsignedByte(5121),
  Short(5122),
  UnsignedShort(5123),
  UnsignedInt(5125),
  Float(5126);

  companion object {
    @JsonCreator
    fun forValue(value: String) =
        ComponentType.values().first { it.value.toString() == value }
  }
}

enum class AttributeType {
  COLOR_0,
  JOINTS_0,
  JOINTS_1,
  JOINTS_2,
  NORMAL,
  POSITION,
  TANGENT,
  TEXCOORD_0,
  WEIGHTS_0,
  WEIGHTS_1,
  WEIGHTS_2
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

data class MeshInfo2(
    var primitives: List<Primitive>
)

data class Metallic(
    var baseColorFactor: List<Float>,
    var metallicFactor: Float
)

data class MaterialInfo(
    var pbrMetallicRoughness: Metallic,
    var emissiveFactor: List<Float>?
)

data class GltfInfo(
    var accessors: List<Accessor>,
    var bufferViews: List<BufferView>,
    var buffers: List<BufferInfo>,
    var meshes: List<MeshInfo2>,
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

  val result = info.meshes.flatMap { it.primitives }.map { primitive ->

    val indexAccessor = info.accessors[primitive.indices]
    val triangleCount = indexAccessor.count / 3
    val vertexCount = info.accessors[primitive.attributes[AttributeType.POSITION]!!].count

    val indexCount = triangleCount * 3
    val indices = BufferUtils.createIntBuffer(indexCount)
    val vertices = BufferUtils.createFloatBuffer(3 * 2 * vertexCount)

    if (true) {
      val bufferView = info.bufferViews[primitive.indices]
      buffer.position(bufferView.byteOffset)

      if (indexAccessor.componentType == ComponentType.UnsignedShort.value) {
        val intBuffer = buffer.asShortBuffer()
        for (i in 0 until indexAccessor.count) {
          val value = intBuffer.get().toInt() and 0xFF
//          println(value)
          indices.put(value)
        }
      } else {
        for (i in 0 until indexAccessor.count) {
          val value = buffer.get().toInt() and 0xFF
//          println(value)
          indices.put(value)
        }
      }
    }

    for (attribute in primitive.attributes) {
      val bufferView = info.bufferViews[attribute.value]
      buffer.position(bufferView.byteOffset)
      val mappedAttribute = attributeMap[attribute.key]
      if (mappedAttribute == null)
        continue

      val vertexAttribute = vertexSchema.getAttribute(mappedAttribute)
      vertices.position()
      for (i in 0 until vertexCount) {
        vertices.position(vertexAttribute.offset + i * vertexSchema.floatSize)
        for (x in 0 until 3) {
          val value = buffer.getFloat()
//          println(value)
          vertices.put(value)
        }
      }
    }

    vertices.position(0)
    indices.position(0)

    val materialSource = info.materials[primitive.material]
    val color = arrayToVector4(materialSource.pbrMetallicRoughness.baseColorFactor)
    val glow = if (materialSource.emissiveFactor != null)
      color.x / materialSource.emissiveFactor!!.first()
    else
      0f

    ModelElement(
        mesh = SimpleTriangleMesh(vertexSchema, vertices, indices),
        material = Material(
            color = color,
            glow = glow
        )
    )
  }

//  if (result.size > 3)
//    return listOf(result[1])
  return result
}