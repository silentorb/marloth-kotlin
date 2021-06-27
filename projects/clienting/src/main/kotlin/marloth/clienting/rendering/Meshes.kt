package marloth.clienting.rendering

import marloth.scenery.enums.MeshId
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.glowing.*
import silentorb.mythic.lookinglass.Armature
import silentorb.mythic.lookinglass.LodRanges
import silentorb.mythic.lookinglass.ModelMesh
import silentorb.mythic.lookinglass.meshes.VertexSchemas
import silentorb.mythic.lookinglass.meshes.createBillboardMesh
import silentorb.mythic.lookinglass.meshes.createLineMesh
import silentorb.mythic.lookinglass.meshes.createModelElements
import silentorb.mythic.lookinglass.meshes.loading.loadGltf
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.resource_loading.listFiles
import silentorb.mythic.scenery.MeshName
import java.nio.file.Path

fun getModelFilenames(): List<Path> {
  val modelRoot = getUrlPath("models")
  val files = listFiles(modelRoot)
  return files
}

fun getMeshFilenames(): List<Path> {
  val modelRoot = getUrlPath("gltf")
  val files = listFiles(modelRoot)
  return files
}

fun importedMeshes(vertexSchemas: VertexSchemas) =
    getMeshFilenames()
        .map { it.fileName.toString() }
        .map { loadGltf(vertexSchemas, it, "gltf/" + it + "/" + it) }

fun createHollowCircleMesh(vertexSchema: VertexSchema, resolution: Int): GeneralMesh {
  val values2d = createCircleList(1f, resolution, 0f, -1f)
  val values3d = (0 until values2d.size step 2).flatMap { listOf(values2d[it], values2d[it + 1], 0f) }

  return GeneralMesh(
      vertexSchema = vertexSchema,
      primitiveType = PrimitiveType.loops,
      vertexBuffer = newVertexBuffer(vertexSchema).load(createFloatBuffer(values3d)),
      count = resolution
  )
}

//fun newImpLibrary() =
//    listOf(
//        defaultImpNamespace(),
//        standardLibrary(),
////      auraLibrary(),
//        texturingLibrary(),
//    )

val defaultLodRanges: LodRanges = listOf(
    60f,
    25f,
    10f
)

//fun updateAsyncMeshLoading(vertexSchema: VertexSchema) = updateAsyncLoading(::sampleModels, meshesToGpu(vertexSchema))

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshName, ModelMesh>, List<Armature>> {
  val imports = importedMeshes(vertexSchemas)
  val importedMeshes = imports.flatMap { it.meshes }.associateBy { it.id }
  val customMeshes = mapOf(
      MeshId.hollowCircle to createHollowCircleMesh(vertexSchemas.flat, 64),
      "line" to createLineMesh(vertexSchemas.flat),
      "billboard" to createBillboardMesh(vertexSchemas.textured)
  )
      .mapValues { createModelElements(it.value) }
      .mapValues { ModelMesh(it.key, it.value) }
  val meshes = importedMeshes + customMeshes

  val armatures = imports.flatMap { it.armatures }
  return Pair(meshes, armatures)
}
