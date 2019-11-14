package rendering.meshes.loading

import mythic.breeze.*
import mythic.glowing.GeneralMesh
import mythic.glowing.VertexAttributeDetail
import mythic.glowing.newVertexBuffer
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import rendering.*
import rendering.meshes.AttributeName
import rendering.meshes.VertexSchema
import rendering.meshes.VertexSchemas
import scenery.*
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

typealias VertexConverter = (ByteBuffer, FloatBuffer, VertexAttributeDetail, Int) -> Unit

fun createVertexConverter(info: GltfInfo, transformBuffer: ByteBuffer, boneMap: BoneMap, meshIndex: Int): VertexConverter {
  val meshNode = info.nodes.first { it.mesh == meshIndex }
  val skin = if (meshNode.skin != null) info.skins?.get(meshNode.skin) else null
//  println("Skinned: ${meshNode.name} ${skin != null}")

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
      if (attribute.name == AttributeName.weights.name) {
        lastWeights = (0 until attribute.size).map {
          val value = buffer.getFloat()
          vertices.put(value)
          value
        }
      } else if (attribute.name == AttributeName.joints.name) {
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

enum class VertexPacking {
  interleaved,
  noninterleaved
}

fun loadVertices(buffer: ByteBuffer, info: GltfInfo, vertexSchema: VertexSchema, primitive: Primitive,
                 converter: VertexConverter): Pair<FloatBuffer, VertexPacking> {
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
        val stride = if (bufferView.byteStride != 0)
          bufferView.byteStride
        else
          attribute.size * 4

        buffer.position(bufferView.byteOffset + attributeAccessor.byteOffset + i * stride)
        converter(buffer, vertices, attribute, i)
      } else {
        for (x in 0 until attribute.size) {
          vertices.put(0f)
        }
      }
    }
  }
  val packing = if (attributes.first().second?.byteStride == 0)
    VertexPacking.noninterleaved
  else
    VertexPacking.interleaved

  return Pair(vertices, packing)
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
  val materialSource = info.materials!![materialIndex]
  val details = materialSource.pbrMetallicRoughness
  val color = details.baseColorFactor
  val glow = if (materialSource.emissiveFactor != null && materialSource.emissiveFactor.first() != 0f)
    materialSource.emissiveFactor.first()
  else
    0f

  val texture = if (details.baseColorTexture == null) {
    null
  } else {
    val gltfTexture = info.textures!![details.baseColorTexture.index]
    val gltfImage = info.images!![gltfTexture.source]
    toCamelCase(gltfImage.uri.substringBeforeLast(".").substringAfterLast("/").substringAfterLast("\\"))
  }

  return Material(
      color = color,
      glow = glow,
      texture = texture,
      shading = true
  )
}

fun formatArmatureName(name: String): String =
    toCamelCase(name.replace("rig_", ""))

fun getParentBone(info: GltfInfo, nodeIndex: Int, boneMap: BoneMap): Int? {
  val node = info.nodes[nodeIndex]
  return if (node.extras != null && node.extras.containsKey("parent")) {
    val rawName = node.extras["parent"] as String
    val parentName = rawName.replace("rig_", "")
    boneMap.values.firstOrNull { it.name == parentName }?.index
  } else
    null
}

fun loadPrimitiveMesh(buffer: ByteBuffer, info: GltfInfo, vertexSchemas: VertexSchemas, primitive: Primitive,
                      converter: VertexConverter): GeneralMesh {
  val vertexSchema = if (primitive.attributes.containsKey(AttributeType.JOINTS_0))
    vertexSchemas.animated
  else if (primitive.attributes.containsKey(AttributeType.TEXCOORD_0))
    vertexSchemas.textured
  else
    vertexSchemas.imported

  val (vertices, packing) = loadVertices(buffer, info, vertexSchema, primitive, converter)
  val indices = loadIndices(buffer, info, primitive)

  vertices.position(0)
  indices.position(0)

  return GeneralMesh(
      vertexSchema = vertexSchema,
      vertexBuffer = newVertexBuffer(vertexSchema, packing == VertexPacking.interleaved).load(vertices),
      indices = indices
  )
}

fun convertChannelType(source: String): ChannelType =
    when (source) {
      "rotation" -> ChannelType.rotation
      "scale" -> ChannelType.scale
      "translation" -> ChannelType.translation
      else -> throw Error("Unsupported channel type: " + source)
    }

fun loadChannel(target: ChannelTarget, buffer: ByteBuffer, info: GltfInfo, sampler: AnimationSampler, boneIndex: BoneNode): mythic.breeze.SkeletonAnimationChannel {
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

  return SkeletonAnimationChannel(
      target = mythic.breeze.ChannelTarget(boneIndex.index, convertChannelType(target.path)),
      keys = times.zip(values) { time, value -> Keyframe(time, value) }
  )
}

fun loadAnimation(buffer: ByteBuffer, info: GltfInfo, source: IndexedAnimation, boneIndexMap: Map<Int, BoneNode>): SkeletonAnimation {
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

  return SkeletonAnimation(
      name = source.name,
      channels = channels,
      channelMap = mapChannels(channels),
      duration = duration
  )
}

fun loadAnimations(buffer: ByteBuffer, info: GltfInfo, animations: List<IndexedAnimation>, bones: List<Bone>, boneIndexMap: Map<Int, BoneNode>): AnimationMap {
  return animations.mapNotNull { source ->
    val name = formatArmatureName(source.name)
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

fun getMeshName(info: GltfInfo, nodeIndex: Int): MeshName? {
  val parent = info.nodes.firstOrNull { it.children != null && it.children.contains(nodeIndex) }
  val node = info.nodes[nodeIndex]
  val name = if (parent != null && parent.name != "rig")
    parent.name
  else
    node.name

  return toCamelCase(name)
}

@Suppress("UNCHECKED_CAST")
private fun parseVector3(source: Any?): Vector3 {
  val dimensions = source as List<Double>
  return Vector3(dimensions[0].toFloat(), dimensions[1].toFloat(), dimensions[2].toFloat())
}

fun loadBoundingShape(shapeProperty: Map<String, Any>): Shape? {
  val source = shapeProperty as Map<String, Any>
  val type = source["type"] as String?
  val shape = when (type) {

    "composite" -> {
      @Suppress("UNCHECKED_CAST")
      val shapes = source.getValue("children") as List<Map<String, Any>>
      val shapes2 = shapes.mapNotNull(::loadBoundingShape)
      CompositeShape(
          shapes = shapes2
      )
    }

    "cylinder" -> Cylinder(
        radius = parseFloat(source["radius"]),
        height = parseFloat(source["height"])
    )

    "box" -> {
      Box(
          halfExtents = parseVector3(source["dimensions"]) * 0.5f
      )
    }

    else -> null
  }
  val offset = if (source.containsKey("offset"))
    parseVector3(source["offset"])
  else
    null

  return if (shape != null && offset != null)
    ShapeOffset(transform = Matrix().translate(offset), shape = shape)
  else
    shape
}

fun loadBoundingShapeFromNode(node: Node): Shape? {
  val shapeProperty = node.extras?.get("bounds")
  return if (shapeProperty == null)
    null
  else {
    @Suppress("UNCHECKED_CAST")
    val source = shapeProperty as Map<String, Any>
    return loadBoundingShape(source)
  }
}

fun gatherChildLights(info: GltfInfo, node: Node): List<Light> {
  if (info.extensions != null) {
    val k = 0
  }
  val lights = info.extensions?.KHR_lights_punctual?.lights
  return if (node.children == null || lights == null)
    listOf()
  else
    info.nodes.mapNotNull { childNode ->
      val lightIndex = childNode.extensions?.KHR_lights_punctual?.light
      if (lightIndex == null)
        null
      else {
        val light = lights[lightIndex]
        Light(
            type = LightType.values().first { it.name == light.type.name },
            color = Vector4f(light.color, light.intensity / 100f),
            position = childNode.translation ?: Vector3.zero,
            direction = null,
            range = 15f
        )
      }
    }
}

fun loadMeshes(info: GltfInfo, buffer: ByteBuffer, vertexSchemas: VertexSchemas, boneMap: BoneMap): List<ModelMesh> {
  return info.meshes
      .mapIndexedNotNull { meshIndex, mesh ->
        val nodeIndex = info.nodes.indexOfFirst { it.mesh == meshIndex }
        val id = getMeshName(info, nodeIndex)
        if (id == null)
          null
        else {
          val name2 = mesh.name.replace(".001", "")

          val parentBone = getParentBone(info, nodeIndex, boneMap)
          val primitives = mesh.primitives.map { primitiveSource ->
            val material = loadMaterial(info, primitiveSource.material)
            val converter = createVertexConverter(info, buffer, boneMap, meshIndex)
            rendering.meshes.Primitive(
                mesh = loadPrimitiveMesh(buffer, info, vertexSchemas, primitiveSource, converter),
                transform = null,
                material = material,
                name = name2,
                parentBone = parentBone
            )
          }
          val node = info.nodes[nodeIndex]

          ModelMesh(
              id = id,
              primitives = primitives,
              lights = gatherChildLights(info, node),
              bounds = loadBoundingShapeFromNode(node)
          )
        }
      }
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

  val meshes = loadMeshes(info, buffer, vertexSchemas, boneMap)

  return ModelImport(meshes = meshes, armatures = armatures)
}
