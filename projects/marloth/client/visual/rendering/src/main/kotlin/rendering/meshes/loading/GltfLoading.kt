package rendering.meshes.loading

import mythic.breeze.*
import mythic.glowing.SimpleTriangleMesh
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.lwjgl.BufferUtils
import rendering.*
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

fun loadIndices(buffer: ByteBuffer, info: GltfInfo, primitive: Primitive): IntBuffer {
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

fun loadVertices(buffer: ByteBuffer, info: GltfInfo, vertexSchema: VertexSchema, primitive: Primitive): FloatBuffer {
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
    for (i in 0 until vertexCount) {
//      val slice = buffer.slice().asFloatBuffer()
//      slice.limit(3)
//      buffer.position(buffer.position() + 4 * 3)
      vertices.position(vertexAttribute.offset + i * vertexSchema.floatSize)
//      vertices.put(slice)
      for (x in 0 until 3) {
        val value = buffer.getFloat()
        vertices.put(value)
      }
    }
  }
  return vertices
}

fun loadPrimitive(primitive: Primitive, name: String, buffer: ByteBuffer, info: GltfInfo,
                  vertexSchema: VertexSchema): rendering.meshes.Primitive {
  val vertices = loadVertices(buffer, info, vertexSchema, primitive)
  val indices = loadIndices(buffer, info, primitive)

  vertices.position(0)
  indices.position(0)

  val materialSource = info.materials[primitive.material]
  val color = materialSource.pbrMetallicRoughness.baseColorFactor // arrayToVector4()
  val glow = if (materialSource.emissiveFactor != null && materialSource.emissiveFactor.first() != 0f)
    color.x / materialSource.emissiveFactor.first()
  else
    0f

  return Primitive(
      mesh = SimpleTriangleMesh(vertexSchema, vertices, indices),
      material = Material(
          color = color,
          glow = glow
      ),
      name = name
  )
}

data class Node(
    val name: String,
    val rotation: Quaternion,
    val translation: Vector3,
    var children: List<Node> = listOf(),
    var parent: Int? = null
)

fun convertNode(indexedNode: IndexedNode): Node {
  val rotation = if (indexedNode.rotation != null)
    Quaternion(indexedNode.rotation.x, indexedNode.rotation.y, indexedNode.rotation.z, indexedNode.rotation.w)
  else
    Quaternion()

  val translation = if (indexedNode.translation != null)
    indexedNode.translation
  else
    Vector3()

  return Node(indexedNode.name, rotation, translation)
}

fun loadNodes(indexedNodes: List<IndexedNode>): List<Node> {
  val nodes = indexedNodes.map { convertNode(it) }
  val indexedIterator = indexedNodes.iterator()
  nodes.forEachIndexed { i, node ->
    val indexedNode = indexedIterator.next()
    if (indexedNode.children != null) {
      node.children = indexedNode.children.map {
        nodes[it]
      }
      node.children.forEach { it.parent = i }
    }
  }

  return nodes
}

fun loadKeyframes(inputIndex: Int, outputIndex: Int, info: GltfInfo) {

}

fun convertChannelType(source: String): ChannelType =
    when (source) {
      "rotation" -> ChannelType.rotation
      "scale" -> ChannelType.scale
      "translation" -> ChannelType.translation
      else -> throw Error("Unsupported channel type: " + source)
    }

fun loadAnimation(source: IndexedAnimation, bones: Map<Int, Bone>): AnimationOld {

  val samplers = source.samplers.map {
    listOf<Keyframe>()
//    AnimationSampler2(
//        it.input,
//        it.output
//    )
  }

  val channels = source.channels.map {
    AnimationChannel2(
        samplers[it.sampler],
        ChannelTarget2(bones[it.target.node]!!, convertChannelType(it.target.path))
    )
  }

  return AnimationOld(
      channels = channels,
      samplers = samplers
  )
}

fun loadAnimations(animations: List<IndexedAnimation>, bones: Map<Int, Bone>): List<AnimationOld> {
  return animations.map { loadAnimation(it, bones) }
}

fun nodeToBone(node: Node) =
    Bone(
        translation = node.translation,
        rotation = node.rotation,
        name = node.name,
        transform = independentTransform
    )

fun createBoneMap(nodes: List<Node>, animations: List<IndexedAnimation>): Map<Int, Bone> =
    animations.flatMap { it.channels.map { it.target.node } }
        .distinct()
        .associate {
          val node = nodes[it]
          Pair(it, nodeToBone(node))
        }

fun loadArmature(info: GltfInfo): Armature? {
  val animations = info.animations
  if (animations == null || animations.none())
    return null

  val nodes = loadNodes(info.nodes)

//  val rootNodes = nodes.filter { it.parent == null }
  val boneMap = createBoneMap(nodes, animations)

  return Armature(
      bones = boneMap.values.toList(),
      animations = listOf()// loadAnimations(animations, boneMap)
  )
}

fun loadGltf(vertexSchemas: VertexSchemas, resourcePath: String): AdvancedModel {
  val info = loadJsonResource<GltfInfo>(resourcePath + ".gltf")
  val directoryPath = resourcePath.split("/").dropLast(1).joinToString("/")
  val buffer = loadGltfByteBuffer(directoryPath, info)
  val vertexSchema = vertexSchemas.imported

  val result = info.meshes.flatMap { mesh -> mesh.primitives.map { Pair(it, mesh.name) } }.map { pair ->
    val (primitive, name) = pair
    loadPrimitive(primitive, name.replace(".001", ""), buffer, info, vertexSchema)
  }

  val armature = loadArmature(info)

  return AdvancedModel(result, armature)
}