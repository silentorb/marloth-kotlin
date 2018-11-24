package rendering

import getResourceUrl
import mythic.quartz.globalProfiler
import rendering.meshes.*
import rendering.meshes.loading.loadGltf
import scenery.MeshId
import java.io.File
import java.nio.file.Paths
import mythic.glowing.VertexSchema as GenericVertexSchema

enum class MeshType {
  bear,
  cylinder,
  child,
  eyeball,
  human,
  humanOld,
  line,
  monster,
  person,
  prisonDoor,
  skeleton,
  skybox,
  sphere,
  cube,
  wallLamp,
}

typealias AdvancedModelGenerator = () -> AdvancedModel

typealias AdvancedModelGeneratorMap = Map<MeshType, AdvancedModelGenerator>

//fun advancedMeshes(vertexSchemas: VertexSchemas): AdvancedModelGeneratorMap {
//  return mapOf(
////      MeshType.skybox to skyboxModel(vertexSchemas)
//  )
//}

fun getMeshFilenames(): Array<File> {
  val modelRoot = getResourceUrl("models")
  return File(modelRoot.toURI()).listFiles()
}

fun importedMeshes(vertexSchemas: VertexSchemas) =
//    mapOf(
//    MeshType.wallLamp to "lamp",
//    MeshType.prisonDoor to "prison_door",
//    MeshType.cube to "cube",
//    MeshType.child to "child",
//    MeshType.person to "person"
//)
    globalProfiler().wrap("meshes") {
      getMeshFilenames()
          .map { it.name }
          .map { loadGltf(vertexSchemas, it, "models/" + it + "/" + it) }
    }

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshId, ModelMesh>, List<Armature>> {
  val imports = importedMeshes(vertexSchemas)
  val meshes = mapOf(
      MeshId.line to createLineMesh(vertexSchemas.flat)
  )
      .mapValues { createModelElements(it.value) }
      .mapValues { ModelMesh(it.key, it.value) }
      .plus(imports.flatMap { it.meshes }.associate { Pair(it.id, it) })

  val armatures = imports.flatMap { it.armatures }
  return Pair(meshes, armatures)

}
