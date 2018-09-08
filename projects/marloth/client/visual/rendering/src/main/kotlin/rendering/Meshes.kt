package rendering

import mythic.breeze.Armature
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import rendering.meshes.*
import rendering.meshes.loading.loadGltf
import rigging.createSkeleton
import rigging.humanAnimations
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
  skeleton,
  skybox,
  sphere,
  cube,
  wallLamp,
}

typealias AdvancedModelGenerator = () -> AdvancedModel

typealias AdvancedModelGeneratorMap = Map<MeshType, AdvancedModelGenerator>

fun standardMeshes(): ModelGeneratorMap = mapOf(
//    MeshType.cube to createCube,
    MeshType.eyeball to createEyeball,
    MeshType.sphere to createSphere
//    MeshType.bear to createCartoonHuman,
//    MeshType.human to createHuman,
//    MeshType.humanOld to createHumanOld,
//    MeshType.monster to createCartoonHuman
//    MeshType.wallLamp to createWallLamp
    )

fun skeletonMesh(vertexSchemas: VertexSchemas): AdvancedModelGenerator = {
  val bones = createSkeleton()
  val (model, weights) = modelFromSkeleton(bones, "Skeleton", Material(Vector4(1f, 1f, 1f, 1f)))
//  val (model, weights) = modelFromSkeleton(bones, "Skeleton", Material(Vector4(0.4f, 0.25f, 0.0f, 1f)))
  val armature = Armature(
      bones = bones,
      originalBones = bones,
      animations = humanAnimations(bones)
  )
  AdvancedModel(
      model = model,
      primitives = modelToMeshes(vertexSchemas, model, weights),
      armature = armature,
      weights = weights
  )
}

fun advancedMeshes(vertexSchemas: VertexSchemas): AdvancedModelGeneratorMap {
  val skeleton = skeletonMesh(vertexSchemas)
  return mapOf(
      MeshType.skeleton to skeleton,
      MeshType.child to skeleton,
      MeshType.skybox to skyboxModel(vertexSchemas)
  )
}

fun importedMeshes(vertexSchemas: VertexSchemas) = mapOf(
    MeshType.wallLamp to "lamp",
    MeshType.cube to "cube"
//    MeshType.child to "girl2/child"
//    MeshType.child to "child/child"
)
    .mapValues { loadGltf(vertexSchemas, "models/" + it.value) }

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap = mapOf(
    MeshType.line to createLineMesh(vertexSchemas.flat),
    MeshType.cylinder to createSimpleMesh(createCylinder(), vertexSchemas.textured)
)
    .mapValues { createModelElements(it.value) }
//    .plus(mapOf(
//        MeshType.sphere to createModelElements(createSimpleMesh(createSphere(), vertexSchemas.standard, Vector4(1f)),
//            Vector4(0.4f, 0.1f, 0.1f, 1f))
//    ))
    .plus(standardMeshes().mapValues {
      modelToMeshes(vertexSchemas, it.value())
    })
    .mapValues { AdvancedModel(it.value) }
    .plus(importedMeshes(vertexSchemas))
    .plus(advancedMeshes(vertexSchemas).mapValues { it.value() })
