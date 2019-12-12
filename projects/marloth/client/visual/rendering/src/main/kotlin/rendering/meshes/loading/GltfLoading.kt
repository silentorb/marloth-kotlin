package rendering.meshes.loading

import mythic.spatial.Vector3
import org.joml.Vector4f
import rendering.Armature
import rendering.Material
import rendering.ModelMesh
import rendering.meshes.VertexSchemas
import rendering.toCamelCase
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType

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

data class ModelImport(
    val meshes: List<ModelMesh>,
    val armatures: List<Armature>
)

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
