package rendering

import mythic.glowing.VertexSchema as GenericVertexSchema
import mythic.spatial.*
import rendering.meshes.*
import rendering.meshes.loading.loadGltf

enum class MeshType {
  bear,
  cylinder,
  child,
  human,
  humanOld,
  line,
  monster,
  sphere,
  cube,
  wallLamp,
}

fun standardMeshes(): ModelGeneratorMap = mapOf(
//    MeshType.cube to createCube,
    MeshType.sphere to createSphere
//    MeshType.bear to createCartoonHuman,
//    MeshType.human to createHuman,
//    MeshType.humanOld to createHumanOld,
//    MeshType.monster to createCartoonHuman
//    MeshType.wallLamp to createWallLamp
)

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap = mapOf(
    MeshType.line to createLineMesh(vertexSchemas.flat),
    MeshType.cylinder to createSimpleMesh(createCylinder(), vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f))
)
    .mapValues { createModelElements(it.value) }
//    .plus(mapOf(
//        MeshType.sphere to createModelElements(createSimpleMesh(createSphere(), vertexSchemas.standard, Vector4(1f)),
//            Vector4(0.4f, 0.1f, 0.1f, 1f))
//    ))
    .plus(standardMeshes().mapValues {
      modelToMeshes(vertexSchemas, it.value())
    })
    .plus(importedMeshes(vertexSchemas))


fun importedMeshes(vertexSchemas: VertexSchemas) = mapOf(
    MeshType.wallLamp to "lamp",
    MeshType.cube to "cube",
//    MeshType.child to "girl2/child"
    MeshType.child to "child/child"
)
    .mapValues { loadGltf(vertexSchemas, "models/" + it.value) }
