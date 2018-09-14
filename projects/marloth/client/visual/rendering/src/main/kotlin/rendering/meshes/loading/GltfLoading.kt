package rendering.meshes.loading

import mythic.breeze.*
import mythic.glowing.SimpleTriangleMesh
import mythic.spatial.*
import org.lwjgl.BufferUtils
import rendering.*
import rendering.meshes.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

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
  val vertices = BufferUtils.createFloatBuffer(vertexSchema.floatSize * vertexCount)

  val attributes = vertexSchema.attributes.map { attribute ->
    val mappedAttribute = attributeMap2[attribute.name]
    if (mappedAttribute == null)
      throw Error("Missing attribute map for " + attribute.name)

    val attributeAccessorIndex = primitive.attributes [mappedAttribute]
    if (attributeAccessorIndex != null) {
      val attributeAccessor = info.accessors[attributeAccessorIndex]
      val bufferView = info.bufferViews[attributeAccessor.bufferView]
      Triple(attributeAccessor, bufferView, attribute.size)
    }
    else
      Triple(null, null, attribute.size)
  }

  for (i in 0 until vertexCount) {
//      val slice = buffer.slice().asFloatBuffer()
//      slice.limit(3)
//      buffer.position(buffer.position() + 4 * 3)
    for ((attributeAccessor, bufferView, componentCount) in attributes) {
      if (attributeAccessor != null && bufferView != null) {
        buffer.position(bufferView.byteOffset + attributeAccessor.byteOffset + i * bufferView.byteStride)
//      vertices.position(attribute.offset + i * vertexSchema.floatSize)
//      vertices.put(slice)
        for (x in 0 until componentCount) {
          val value = buffer.getFloat()
          vertices.put(value)
        }
      }
      else {
        for (x in 0 until componentCount) {
          vertices.put(0f)
        }
      }
    }
  }
  return vertices
}

fun loadPrimitive(primitive: Primitive, name: String, buffer: ByteBuffer, info: GltfInfo,
                  vertexSchemas: VertexSchemas): rendering.meshes.Primitive {
  val vertexSchema = if (primitive.attributes.size == 2)
    vertexSchemas.imported
  else
    vertexSchemas.textured

  val vertices = loadVertices(buffer, info, vertexSchema, primitive)
  val indices = loadIndices(buffer, info, primitive)

  vertices.position(0)
  indices.position(0)

  val materialSource = info.materials[primitive.material]
  val details = materialSource.pbrMetallicRoughness
  val color = details.baseColorFactor // arrayToVector4()
  val glow = if (materialSource.emissiveFactor != null && materialSource.emissiveFactor.first() != 0f)
    color.x / materialSource.emissiveFactor.first()
  else
    0f

  val texture = if (details.baseColorTexture == null) {
    null
  } else {
    val gltfTexture = info.textures!![details.baseColorTexture.index]
    val gltfImage = info.images!![gltfTexture.source]
    gltfImage.uri.substringBeforeLast(".")
  }
  return Primitive(
      mesh = SimpleTriangleMesh(vertexSchema, vertices, indices),
      transform = null,
      material = Material(
          color = color,
          glow = glow,
          texture = texture
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
        translation = Vector3m(node.translation),
        rotation = node.rotation,
        name = node.name,
        transform = independentTransform,
        length = 1f,
        index = -1,
        isGlobal = false
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

  val bones = boneMap.values.toList()
  return Armature(
      bones = bones,
      originalBones = bones,
      animations = listOf()// loadAnimations(animations, boneMap)
  )
}

fun loadGltf(vertexSchemas: VertexSchemas, resourcePath: String): AdvancedModel {
  val info = loadJsonResource<GltfInfo>(resourcePath + ".gltf")
  val directoryPath = resourcePath.split("/").dropLast(1).joinToString("/")
  val buffer = loadGltfByteBuffer(directoryPath, info)

  val result = info.meshes.flatMap { mesh -> mesh.primitives.map { Pair(it, mesh.name) } }.map { pair ->
    val (primitive, name) = pair
    loadPrimitive(primitive, name.replace(".001", ""), buffer, info, vertexSchemas)
  }

  val armature = loadArmature(info)

  return AdvancedModel(primitives = result, armature = armature, weights = mapOf())
}