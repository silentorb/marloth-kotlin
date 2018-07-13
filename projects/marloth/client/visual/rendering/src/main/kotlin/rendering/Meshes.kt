package rendering

import mythic.breeze.Armature
import rendering.meshes.*
import rigging.createSkeleton
import rigging.humanAnimations
import mythic.glowing.VertexSchema as GenericVertexSchema

enum class MeshType {
  bear,
  cylinder,
  child,
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
    MeshType.cube to createCube,
    MeshType.sphere to createSphere,
//    MeshType.bear to createCartoonHuman,
//    MeshType.human to createHuman,
//    MeshType.humanOld to createHumanOld,
//    MeshType.monster to createCartoonHuman
    MeshType.wallLamp to createWallLamp
)

fun skeletonMesh(vertexSchemas: VertexSchemas): AdvancedModelGenerator = {
  val bones = createSkeleton()
  val (model, weights) = modelFromSkeleton(bones, "Skeleton")
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
    .plus(advancedMeshes(vertexSchemas).mapValues { it.value() })
