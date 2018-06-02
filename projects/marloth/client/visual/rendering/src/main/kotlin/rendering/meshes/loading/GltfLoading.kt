package rendering.meshes.loading

import mythic.glowing.SimpleTriangleMesh
import org.lwjgl.BufferUtils
import rendering.Material
import rendering.meshes.*
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun loadGltfByteBuffer(directoryPath: String, info: GltfInfo): ByteBuffer {
  val inputStream = loadBinaryResource(directoryPath + "/" + info.buffers[0].uri)
  val dataStream = DataInputStream(inputStream)
  val buffer = BufferUtils.createByteBuffer(info.buffers[0].byteLength)
  dataStream.use {
    while (dataStream.available() > 0) {
      buffer.put(dataStream.readByte())
    }
  }

  return buffer
}

typealias BufferIterator = (ByteBuffer, Int, (Int) -> Unit) -> Unit

val iterateBytes = { buffer: ByteBuffer, count: Int, action: (Int) -> Unit ->
  for (i in 0 until count) {
    val value = buffer.get().toInt() // and 0xFF
    action(value)
  }
}

val iterateShorts = { buffer: ByteBuffer, count: Int, action: (Int) -> Unit ->
  val intBuffer = buffer.asShortBuffer()
  for (i in 0 until count) {
    val value = intBuffer.get().toInt()// and 0xFF
    action(value)
  }
}

fun selectBufferIterator(componentType: Int): BufferIterator =
    when (componentType) {
      ComponentType.UnsignedByte.value -> iterateBytes
      ComponentType.UnsignedShort.value -> iterateShorts
      else -> throw Error("Not implemented.")
    }

fun parseIndices(buffer: ByteBuffer, info: GltfInfo, primitive: Primitive): IntBuffer {
  val indexAccessor = info.accessors[primitive.indices]
  val bufferView = info.bufferViews[indexAccessor.bufferView]

  val triangleCount = indexAccessor.count / 3
  val indexCount = triangleCount * 3
  val indices = BufferUtils.createIntBuffer(indexCount)
  buffer.position(bufferView.byteOffset)
  val iterator = selectBufferIterator(indexAccessor.componentType)
  iterator(buffer, indexAccessor.count, { indices.put(it) })
  return indices
}

fun parseVertices(buffer: ByteBuffer, info: GltfInfo, vertexSchema: VertexSchema, primitive: Primitive): FloatBuffer {
  val vertexAccessor = info.accessors[primitive.attributes[AttributeType.POSITION]!!]
  val vertexCount = vertexAccessor.count
  val vertices = BufferUtils.createFloatBuffer(3 * 2 * vertexCount)
  for (attribute in primitive.attributes) {
    val attributeAccessor = info.accessors[attribute.value]
    val bufferView = info.bufferViews[attributeAccessor.bufferView]
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
        vertices.put(value)
      }
    }
  }
  return vertices
}

fun loadPrimitive(primitive: Primitive, name: String, buffer: ByteBuffer, info: GltfInfo,
                  vertexSchema: VertexSchema): ModelElement {
  val vertices = parseVertices(buffer, info, vertexSchema, primitive)
  val indices = parseIndices(buffer, info, primitive)

  vertices.position(0)
  indices.position(0)

  val materialSource = info.materials[primitive.material]
  val color = materialSource.pbrMetallicRoughness.baseColorFactor // arrayToVector4()
  val glow = if (materialSource.emissiveFactor != null && materialSource.emissiveFactor.first() != 0f)
    color.x / materialSource.emissiveFactor.first()
  else
    0f

  return ModelElement(
      mesh = SimpleTriangleMesh(vertexSchema, vertices, indices),
      material = Material(
          color = color,
          glow = glow
      ),
      name = name
  )
}

fun loadGltf(vertexSchemas: VertexSchemas, resourcePath: String): ModelElements {
  val info = loadJsonResource<GltfInfo>(resourcePath + ".gltf")
  val directoryPath = resourcePath.split("/").dropLast(1).joinToString("/")
  val buffer = loadGltfByteBuffer(directoryPath, info)
  val vertexSchema = vertexSchemas.imported

  val result = info.meshes.flatMap { mesh -> mesh.primitives.map { Pair(it, mesh.name) } }.map { pair ->
    val (primitive, name) = pair
    loadPrimitive(primitive, name.replace(".001", ""), buffer, info, vertexSchema)
  }

  return result
}