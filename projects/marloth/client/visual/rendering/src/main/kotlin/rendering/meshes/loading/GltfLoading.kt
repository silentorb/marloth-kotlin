package rendering.meshes.loading

import mythic.breeze.*
import mythic.glowing.Drawable
import mythic.glowing.SimpleTriangleMesh
import mythic.glowing.VertexAttributeDetail
import org.lwjgl.BufferUtils
import rendering.*
import rendering.meshes.*
import scenery.AnimationId
import scenery.MeshId
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
  val meshNode = info.nodes.first { it.mesh == meshIndex }
  val skin = if (meshNode.skin != null) info.skins?.get(meshNode.skin) else null
  println("Skinned: ${meshNode.name} ${skin != null}")

  return if (skin != null) {
    if (skin.joints.none())
      throw Error("Invalid mesh skin.")

    val names = boneMap.map { it.value.name }
    val jointMap = skin.joints.mapIndexed { index, jointIndex ->
      Pair(index, jointIndex)
    }.associate { it }

    val transforms = getMatrices(transformBuffer, getOffset(info, skin.inverseBindMatrices), skin.joints.size)
    var lastJoints: List<Int> = listOf()
    var lastWeights: List<Float> = listOf()
    return { buffer, vertices, attribute, vertexIndex ->
      if (attribute.name == AttributeName.weights) {
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
    toCamelCase(gltfImage.uri.substringBeforeLast("."))
  }

  return Material(
      color = color,
      glow = glow,
      texture = texture
  )
}

fun getParentBone(info: GltfInfo, nodeIndex: Int, boneMap: BoneMap): Int? {
  val node = info.nodes[nodeIndex]
  return if (node.extras != null && node.extras.containsKey("parent")) {
    val parentName = node.extras["parent"] as String
    boneMap.values.firstOrNull { it.name == parentName }?.index
  } else
    null
}

fun loadPrimitive(buffer: ByteBuffer, info: GltfInfo, vertexSchemas: VertexSchemas, primitive: Primitive,
                  converter: VertexConverter): Drawable {
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

  return SimpleTriangleMesh(vertexSchema, vertices, indices)
}

fun convertChannelType(source: String): ChannelType =
    when (source) {
      "rotation" -> ChannelType.rotation
      "scale" -> ChannelType.scale
      "translation" -> ChannelType.translation
      else -> throw Error("Unsupported channel type: " + source)
    }

fun loadChannel(target: ChannelTarget, buffer: ByteBuffer, info: GltfInfo, sampler: AnimationSampler, boneIndex: BoneNode): mythic.breeze.AnimationChannel {
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
  val n = source.channels.map { info.nodes[it.target.node] }
  val channels = source.channels
      .filter { it.target.path != "scale" }
      .mapNotNull {
        val boneIndex = boneIndexMap[it.target.node]
        if (boneIndex == null)
          null
        else {
          val channel = loadChannel(it.target, buffer, info, source.samplers[it.sampler], boneIndex)

          val lastTime = channel.keys.last().time
          if (lastTime > duration)
            duration = lastTime

          channel
        }
      }

  val d = source.channels.map {
    it.target.node
  }
      .distinct()
      .map { Pair(it, info.nodes[it]) }

  if (source.name == "metarig_walk") {
    val k = 0
  }
  return Animation(
      name = source.name,
      channels = channels,
      channelMap = mapChannels(channels),
      duration = duration
  )
}

fun loadAnimations(buffer: ByteBuffer, info: GltfInfo, animations: List<IndexedAnimation>, bones: List<Bone>, boneIndexMap: Map<Int, BoneNode>): AnimationMap {
  return animations.mapNotNull { source ->
    val name = toCamelCase(source.name.replace("rig_", ""))
    val key = AnimationId.values().firstOrNull { it.name == name }
    if (key != null)
      Pair(key, loadAnimation(buffer, info, source, boneIndexMap))
    else
      null
  }
      .associate { it }
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

fun getSockets(nodes: List<Node>): SocketMap =
    nodes.mapIndexedNotNull { index, node ->
      val socket = node.extras?.get("socket")
      if (socket is String)
        Pair(socket, index)
      else
        null
    }
        .associate { it }

fun getAncestors(nodes: List<Node>, bone: Int): List<Int> {
  val parent = nodes.indexOfFirst { it.children != null && it.children.contains(bone) }
  return if (parent == -1)
    listOf()
  else
    listOf(parent).plus(getAncestors(nodes, parent))
}

fun getBoneMap(info: GltfInfo, additionalBones: Collection<Int>): BoneMap {
  val skins = info.skins
  if (skins == null)
    return mapOf()

  val deformingBones = skins
      .flatMap { skin -> skin.joints }
      .distinct()

  val ancestors = deformingBones
      .flatMap { getAncestors(info.nodes, it) }
      .distinct()

  val deformingBonesAndAncestors = deformingBones
      .plus(ancestors)
      .plus(additionalBones)
      .distinct()

  val root = info.nodes.indexOfFirst { it.name == "rig" }
  val levelMap = gatherBoneHierarchy(info.nodes, root)
  val orderMap = orderBoneHierarchy(levelMap)
      .filter {
        deformingBonesAndAncestors.contains(it.originalIndex)
      }

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

fun loadArmature(buffer: ByteBuffer, info: GltfInfo, filename: String, boneMap: BoneMap, socketMap: SocketMap): Armature? {
  if (info.animations == null || info.animations.none())
    return null
  val bones = boneMap.map { (_, item) ->
    val node = info.nodes[item.originalIndex]
    nodeToBone(node, item.index, item.parent)
  }

  return Armature(
      id = getArmatureId(filename),
      bones = bones,
      animations = loadAnimations(buffer, info, info.animations, bones, boneMap),
      transforms = transformSkeleton(bones),
      sockets = socketMap
  )
}

data class ModelImport(
    val meshes: List<ModelMesh>,
    val armatures: List<Armature>
)

fun getMeshId(info: GltfInfo, nodeIndex: Int): MeshId? {
  val parent = info.nodes.firstOrNull { it.children != null && it.children.contains(nodeIndex) }
  val node = info.nodes[nodeIndex]
  val name = if (parent != null && parent.name != "rig")
    parent.name
  else
    node.name

  return getMeshId(name)
}

fun loadGltf(vertexSchemas: VertexSchemas, filename: String, resourcePath: String): ModelImport {
  val info = loadJsonResource<GltfInfo>(resourcePath + ".gltf")
  val directoryPath = resourcePath.split("/").dropLast(1).joinToString("/")
  val buffer = loadGltfByteBuffer(directoryPath, info)

  val originalSocketMap = if (info.skins != null)
    getSockets(info.nodes)
  else
    mapOf()

  val boneMap = if (info.skins != null)
    getBoneMap(info, originalSocketMap.values)
  else
    mapOf()

  val newSocketMap = originalSocketMap.mapValues { (_, index) ->
    boneMap[index]!!.index
  }

  val armatures = if (info.animations == null || info.animations.none())
    listOf()
  else
    listOf(loadArmature(buffer, info, filename, boneMap, newSocketMap)).mapNotNull { it }

  val meshes = info.meshes
      .mapIndexed { index, mesh ->
        mesh.primitives.map { Triple(it, index, mesh) }
      }
      .flatten()
      .mapNotNull { (primitiveSource, meshIndex, mesh) ->
        val nodeIndex = info.nodes.indexOfFirst { it.mesh == meshIndex }
        val id = getMeshId(info, nodeIndex)
        if (id == null)
          null
        else {
          val name2 = mesh.name.replace(".001", "")

          val parentBone = getParentBone(info, nodeIndex, boneMap)
          val material = loadMaterial(info, primitiveSource.material)
          val converter = createVertexConverter(info, buffer, boneMap, meshIndex)
          val primitive = Primitive(
              mesh = loadPrimitive(buffer, info, vertexSchemas, primitiveSource, converter),
              transform = null,
              material = material,
              name = name2,
              parentBone = parentBone
          )

          Pair(id, primitive)
        }
      }
      .groupBy { it.first }
      .map { ModelMesh(it.key, it.value.map { it.second }) }

  return ModelImport(meshes = meshes, armatures = armatures)
}
