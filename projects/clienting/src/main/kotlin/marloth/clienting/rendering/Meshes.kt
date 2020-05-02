package marloth.clienting.rendering

import marloth.scenery.enums.MeshId
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.glowing.PrimitiveType
import silentorb.mythic.glowing.createFloatBuffer
import silentorb.mythic.glowing.newVertexBuffer
import silentorb.mythic.lookinglass.Armature
import silentorb.mythic.lookinglass.ModelMesh
import silentorb.mythic.lookinglass.getResourceUrl
import silentorb.mythic.lookinglass.meshes.*
import silentorb.mythic.lookinglass.meshes.loading.loadGltf
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Pi
import java.io.File

fun getMeshFilenames(): Array<File> {
  val modelRoot = getResourceUrl("models")
  val files = File(modelRoot!!.toURI()).listFiles()
  return files
}

fun importedMeshes(vertexSchemas: VertexSchemas) =
    getMeshFilenames()
        .map { it.name }
        .map { loadGltf(vertexSchemas, it, "models/" + it + "/" + it) }
//    }

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

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshName, ModelMesh>, List<Armature>> {
  val imports = importedMeshes(vertexSchemas)
  val meshes = mapOf(
      MeshId.hollowCircle to createHollowCircleMesh(vertexSchemas.flat, 64),
      "line" to createLineMesh(vertexSchemas.flat),
      "billboard" to createBillboardMesh(vertexSchemas.billboard)
  )
      .mapValues { createModelElements(it.value) }
      .mapValues { ModelMesh(it.key, it.value) }
      .plus(imports.flatMap { it.meshes }.associate { Pair(it.id, it) })

  val armatures = imports.flatMap { it.armatures }
  return Pair(meshes, armatures)

}
