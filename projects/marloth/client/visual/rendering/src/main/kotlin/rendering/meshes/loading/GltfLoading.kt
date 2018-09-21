package rendering.meshes.loading

import mythic.breeze.*
import mythic.glowing.SimpleTriangleMesh
import mythic.glowing.VertexAttributeDetail
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

typealias VertexConverter = (ByteBuffer, FloatBuffer, VertexAttributeDetail<AttributeName>, Int) -> Unit

fun createVertexConverter(info: GltfInfo, transformBuffer: ByteBuffer, boneMap: BoneMap, meshIndex: Int): VertexConverter {
  val node = info.nodes.firstOrNull { it.mesh == meshIndex && it.skin != null }
  val skin = info.skins?.get(node?.skin!!)

  return if (skin != null) {
    val jointMap = skin.joints.mapIndexed { index, jointIndex ->
      Pair(index, jointIndex)
    }.associate { it }

    val transforms = getMatrices(transformBuffer, getOffset(info, skin.inverseBindMatrices), skin.joints.size)
    var lastJoints: List<Int> = listOf()
    var lastWeights: List<Float> = listOf()
    return { buffer, vertices, attribute, vertexIndex ->
      if (attribute.name == AttributeName.weights) {
        if (node?.name == "hair" && vertexIndex == 44) {
          val k = 0
        }
        lastWeights = (0 until attribute.size).map {
          val value = buffer.getFloat()
          vertices.put(value)
          value
        }
      } else if (attribute.name == AttributeName.joints) {
        lastJoints = (0 until attribute.size).map {
          val position = buffer.position()
          val byteValue = buffer.get()
          val value = byteValue.toInt() and 0xFF
          val jointIndex = jointMap[value]!!
          val converted = boneMap[jointIndex]!!.index
          if (node?.name == "hair" && vertexIndex == 44) {
            val k = position
          }
          vertices.put(converted.toFloat())
          value
        }
//      } else if (attribute.name == AttributeName.position) {
//        val originalVertex = getVector3(buffer)
//        val transformedVertex = lastJoints.foldIndexed(Vector3()) { i, a, b ->
//          a + originalVertex.transform(transforms[b]) * lastWeights [i]
//        }
//        vertices.put(transformedVertex)
      } else {
        for (x in 0 until attribute.size) {
          val value = buffer.getFloat()
          vertices.put(value)
        }
      }
    }
  } else {
    { buffer, vertices, attribute, _ ->
      for (x in 0 until attribute.size) {
        val value = buffer.getFloat()
        vertices.put(value)
      }
    }
  }
}

fun loadVertices(buffer: ByteBuffer, info: GltfInfo, vertexSchema: VertexSchema, primitive: Primitive,
                 converter: VertexConverter): FloatBuffer {
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
      Triple(attributeAccessor, bufferView, attribute)
    } else
      Triple(null, null, attribute)
  }

  for (i in 0 until vertexCount) {
    for ((attributeAccessor, bufferView, attribute) in attributes) {
      if (attributeAccessor != null && bufferView != null) {
        buffer.position(bufferView.byteOffset + attributeAccessor.byteOffset + i * bufferView.byteStride)
        converter(buffer, vertices, attribute, i)
      } else {
        for (x in 0 until attribute.size) {
          vertices.put(0f)
        }
      }
    }
  }
  return vertices
}

typealias SkinMap = Map<Int, Int>

fun mapSkinIndices(info: GltfInfo, node: Node, boneMap: Map<Int, BoneNode>): SkinMap {
  val skin = info.skins!![node.skin!!]
  return skin.joints.mapIndexed { index, jointIndex ->
    Pair(index, boneMap[jointIndex]!!.index)
  }
      .associate { it }
}

fun loadMaterial(info: GltfInfo, materialIndex: Int): Material {
  val materialSource = info.materials[materialIndex]
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

  return Material(
      color = color,
      glow = glow,
      texture = texture
  )
}

fun loadPrimitive(buffer: ByteBuffer, info: GltfInfo, vertexSchemas: VertexSchemas, primitive: Primitive,
                  name: String, converter: VertexConverter): rendering.meshes.Primitive {
  val vertexSchema = if (primitive.attributes.containsKey(AttributeType.JOINTS_0))
    vertexSchemas.animated
  else if (primitive.attributes.containsKey(AttributeType.TEXCOORD_0))
    vertexSchemas.textured
  else
    vertexSchemas.imported

  val vertices = loadVertices(buffer, info, vertexSchema, primitive, converter)
  val indices = loadIndices(buffer, info, primitive)

  vertices.position(0)
  indices.position(0)

  val material = loadMaterial(info, primitive.material)

  return Primitive(
      mesh = SimpleTriangleMesh(vertexSchema, vertices, indices),
      transform = null,
      material = material,
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
                boneIndexMap: Map<Int, BoneNode>): mythic.breeze.AnimationChannel {
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
      target = mythic.breeze.ChannelTarget(boneIndex.index, convertChannelType(target.path)),
      keys = times.zip(values) { time, value -> Keyframe(time, value) }
  )
}

fun loadAnimation(buffer: ByteBuffer, info: GltfInfo, source: IndexedAnimation, boneIndexMap: Map<Int, BoneNode>): Animation {
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

fun loadAnimations(buffer: ByteBuffer, info: GltfInfo, animations: List<IndexedAnimation>, bones: List<Bone>, boneIndexMap: Map<Int, BoneNode>): List<Animation> {
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
    val name: String,
    val originalIndex: Int,
    val level: Int,
    val parent: Int
)

data class BoneNode(
    val name: String,
    val index: Int,
    val originalIndex: Int,
    val parent: Int,
    val originalParent: Int
)

typealias BoneMap = Map<Int, BoneNode>

fun gatherBoneHierarchy(nodes: List<Node>, root: Int, level: Int = 0, parent: Int = -1): List<InitialBoneNode> {
  val node = nodes[root]
  val children = node.children
  val descendents: List<InitialBoneNode> = if (children == null) listOf() else children.flatMap { child ->
    gatherBoneHierarchy(nodes, child, level + 1, root).toList()
  }

  return listOf(InitialBoneNode(
      name = node.name,
      originalIndex = root,
      level = level,
      parent = parent
  ))
      .plus(descendents)
}

fun orderBoneHierarchy(levelMap: List<InitialBoneNode>): List<InitialBoneNode> {
  val top = levelMap.sortedByDescending { it.level }.first().level
  return (0..top).flatMap { level ->
    levelMap.filter { it.level == level }
  }
}

fun loadBoneMap(info: GltfInfo): BoneMap {
  val root = info.nodes.indexOfFirst { it.name == "metarig" }
  val levelMap = gatherBoneHierarchy(info.nodes, root)
  val orderMap = orderBoneHierarchy(levelMap)
  val result = orderMap
      .mapIndexed { i,
                    item ->
        Pair(item.originalIndex, BoneNode(
            name = item.name,
            index = i,
            originalIndex = item.originalIndex,
            parent = if (item.parent == -1) -1 else orderMap.indexOfFirst { it.originalIndex == item.parent },
            originalParent = item.parent
        ))
      }
      .associate { it }

  return result
}

fun loadArmature(buffer: ByteBuffer, info: GltfInfo, boneMap: BoneMap): Armature? {
  if (info.animations == null || info.animations.none())
    return null
  logBuffer(buffer, info)
  val bones = boneMap.map { (_, item) ->
    val node = info.nodes[item.originalIndex]
    nodeToBone(node, item.index, item.parent)
  }

  return Armature(
      bones = bones,
      animations = loadAnimations(buffer, info, info.animations, bones, boneMap),
      transforms = transformSkeleton(bones)
  )
}

fun loadGltf(vertexSchemas: VertexSchemas, resourcePath: String): AdvancedModel {
  val info = loadJsonResource<GltfInfo>(resourcePath + ".gltf")
  val directoryPath = resourcePath.split("/").dropLast(1).joinToString("/")
  val buffer = loadGltfByteBuffer(directoryPath, info)

  val boneMap = if (info.skins != null)
    loadBoneMap(info)
  else
    mapOf()

  val armature = if (info.animations == null || info.animations.none())
    null
  else
    loadArmature(buffer, info, boneMap)

  val primitives = info.meshes
      .mapIndexed { index, mesh ->
        mesh.primitives.map { Triple(it, index, mesh.name) }
      }
      .flatten()
      .map { (primitive, meshIndex, name) ->
        val name2 = name.replace(".001", "")
        val converter = createVertexConverter(info, buffer, boneMap, meshIndex)
        loadPrimitive(buffer, info, vertexSchemas, primitive, name2, converter)
      }

  return AdvancedModel(primitives = primitives, armature = armature)
}