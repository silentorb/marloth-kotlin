package rendering.meshes.loading

import mythic.breeze.*
import mythic.glowing.SimpleTriangleMesh
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

    val attributeAccessorIndex = primitive.attributes[mappedAttribute]
    if (attributeAccessorIndex != null) {
      val attributeAccessor = info.accessors[attributeAccessorIndex]
      val bufferView = info.bufferViews[attributeAccessor.bufferView]
      Triple(attributeAccessor, bufferView, attribute.size)
    } else
      Triple(null, null, attribute.size)
  }

  for (i in 0 until vertexCount) {
    for ((attributeAccessor, bufferView, componentCount) in attributes) {
      if (attributeAccessor != null && bufferView != null) {
        buffer.position(bufferView.byteOffset + attributeAccessor.byteOffset + i * bufferView.byteStride)
        for (x in 0 until componentCount) {
          val value = buffer.getFloat()
          vertices.put(value)
        }
      } else {
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
  val vertexSchema = if (primitive.attributes.containsKey(AttributeType.JOINTS_0))
    vertexSchemas.animated
  else if (primitive.attributes.containsKey(AttributeType.TEXCOORD_0))
    vertexSchemas.textured
  else
    vertexSchemas.imported

  val vertices = loadVertices(buffer, info, vertexSchema, primitive)
  val indices = loadIndices(buffer, info, primitive)

  vertices.position(0)
  indices.position(0)

  val materialSource = info.materials[primitive.material]
  val details = materialSource.pbrMetallicRoughness
  val color = details.baseColorFactor
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

fun convertChannelType(source: String): ChannelType =
    when (source) {
      "rotation" -> ChannelType.rotation
      "scale" -> ChannelType.scale
      "translation" -> ChannelType.translation
      else -> throw Error("Unsupported channel type: " + source)
    }

fun loadChannel(target: ChannelTarget, buffer: ByteBuffer, info: GltfInfo, sampler: AnimationSampler,
                boneIndexMap: Map<Int, Int>): mythic.breeze.AnimationChannel {
  val boneIndex = boneIndexMap[target.node]!!
  val inputAccessor = info.accessors[sampler.input]
  val inputBufferView = info.bufferViews[inputAccessor.bufferView]
  val outputAccessor = info.accessors[sampler.output]
  val outputBufferView = info.bufferViews[outputAccessor.bufferView]
  val times = getFloats(buffer, inputBufferView.byteOffset, inputAccessor.count)
  val values: List<Any> = when (target.path) {
    "translation" -> getVector3List(buffer, outputBufferView.byteOffset, outputAccessor.count)
    "rotation" -> getQuaternions(buffer, outputBufferView.byteOffset, outputAccessor.count)
    "scale" -> getVector3List(buffer, outputBufferView.byteOffset, outputAccessor.count)
    else -> throw Error("Not implemented.")
  }

  return AnimationChannel(
      target = mythic.breeze.ChannelTarget(boneIndex, convertChannelType(target.path)),
      keys = times.zip(values) { time, value -> Keyframe(time, value) }
  )
}

fun loadAnimation(buffer: ByteBuffer, info: GltfInfo, source: IndexedAnimation, boneIndexMap: Map<Int, Int>): Animation {
  var duration = 0f
  val channels = source.channels.map {
    val channel = loadChannel(it.target, buffer, info, source.samplers[it.sampler], boneIndexMap)

    val lastTime = channel.keys.last().time
    if (lastTime > duration)
      duration = lastTime

    channel
  }

  return Animation(
      channels = channels,
      channelMap = mapChannels(channels),
      duration = duration
  )
}

fun loadAnimations(buffer: ByteBuffer, info: GltfInfo, animations: List<IndexedAnimation>, bones: List<Bone>, boneIndexMap: Map<Int, Int>): List<Animation> {
  return animations.map { loadAnimation(buffer, info, it, boneIndexMap) }
}

fun nodeToBone(node: Node, index: Int, parent: Int) =
    Bone(
        name = node.name,
        translation = node.translation!!,
        rotation = loadQuaternion(node.rotation),
        length = 0.1f,
        index = index,
        parent = parent
    )

data class InitialBoneNode(
    val index: Int,
    val level: Int,
    val parent: Int
)

fun gatherBoneHierarchy(nodes: List<Node>, root: Int, level: Int = 0, parent: Int = -1): List<InitialBoneNode> {
  val children = nodes[root].children
  val descendents: List<InitialBoneNode> = if (children == null) listOf() else children.flatMap { child ->
    gatherBoneHierarchy(nodes, child, level + 1, root).toList()
  }

  return listOf(InitialBoneNode(root, level, parent))
      .plus(descendents)
}

fun orderBoneHierarchy(levelMap: List<InitialBoneNode>): List<InitialBoneNode> {
  val top = levelMap.sortedByDescending { it.level }.first().level
  return (0..top).flatMap { level ->
    levelMap.filter { it.level == level }
  }
}

fun loadArmature(buffer: ByteBuffer, info: GltfInfo): Armature? {
  if (info.animations == null || info.animations.none())
    return null

  val root = info.nodes.indexOfFirst { it.name == "metarig" }

  val levelMap = gatherBoneHierarchy(info.nodes, root)
  val orderMap = orderBoneHierarchy(levelMap)
  val bones = orderMap.mapIndexed { i, item ->
    val node = info.nodes[item.index]
    val parent = if (item.parent == -1) -1 else orderMap.indexOfFirst { it.index == item.parent }
    nodeToBone(node, i, parent)
  }
  val boneIndexMap = orderMap
      .mapIndexed { i, it -> Pair(it.index, i) }
      .associate { it }

  return Armature(
      bones = bones,
      originalBones = listOf(),
      animations = loadAnimations(buffer, info, info.animations, bones, boneIndexMap)
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

  val armature = loadArmature(buffer, info)

  return AdvancedModel(primitives = result, armature = armature, weights = mapOf())
}