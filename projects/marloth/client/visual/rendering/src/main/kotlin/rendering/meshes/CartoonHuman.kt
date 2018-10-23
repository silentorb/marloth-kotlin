package rendering.meshes

import mythic.sculpting.ImmutableMesh
import mythic.sculpting.alignToFloor
import mythic.sculpting.createSphere
import mythic.sculpting.translateMesh
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import rendering.Material
import rendering.Model
import rendering.mapMaterialToManyMeshes
import rendering.mapMaterialToMesh

val createCartoonHuman: ModelGenerator = {
  val mesh = ImmutableMesh()
  val skin = Material(Vector4(0.3f, 0.25f, 0.2f, 1f))
  val black = Material(Vector4(0f, 0f, 0f, 1f))
  val headCenter = Vector3(0f, 0f, 0.7f)
  val limbRadius = 0.08f
  val spheres = listOf(

      // Head
      Triple(0.3f, headCenter, skin),
      Triple(0.03f, headCenter + Vector3(0.27f, -0.1f, 0.05f), black),
      Triple(0.03f, headCenter + Vector3(0.27f, 0.1f, 0.05f), black),

      // Torso
      Triple(0.2f, Vector3(0f, 0f, 0.35f), skin),

      // Legs
      Triple(limbRadius, Vector3(0f, -0.1f, 0.1f), skin),
      Triple(limbRadius, Vector3(0f, 0.1f, 0.1f), skin),

      // Arms
      Triple(limbRadius, Vector3(0.04f, -0.25f, 0.4f), skin),
      Triple(limbRadius, Vector3(0.04f, 0.25f, 0.4f), skin),

      // Hands
      Triple(limbRadius, Vector3(0.05f, -0.3f, 0.3f), skin),
      Triple(limbRadius, Vector3(0.05f, 0.3f, 0.3f), skin)
  )
  throw Error("No longer used")
//  val sphereMeshes = spheres.map {
//    val mesh2 = ImmutableMesh()
//    createSphere(mesh2, it.first, 8, 8)
//    translateMesh(mesh2, it.second)
//    mesh.sharedImport(mesh2)
//    Pair(mesh2, it.third)
//  }
//  val v = mesh.distinctVertices.sortedBy { it.z }
//  val v2 = mesh.distinctVertices.sortedBy { -it.z }
////  transformMesh(mesh, Matrix().scale(1.8f))
//
//  alignToFloor(mesh, 0f)
//
//  val materialMap = sphereMeshes.groupBy { it.second }.map { mapMaterialToManyMeshes(it.key, it.value.map { it.first }) }
//  Model(
//      mesh = mesh,
//      groups = materialMap
//  )
}

//val createMonster: ModelGenerator = {
//  val (mesh, info) = createHumanMesh()
//  Model(
//      mesh = mesh,
//      info = info,
//      groups = listOf(mapMaterialToMesh(Material(Vector4(0.25f, 0.25f, 0.25f, 1f)), mesh)
//      ))
//}