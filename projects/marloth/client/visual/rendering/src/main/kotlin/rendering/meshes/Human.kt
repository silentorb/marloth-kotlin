package rendering.meshes

import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import rendering.Material
import rendering.Model
import rendering.ModelGenerator
import rendering.mapMaterial

data class HeadPorts(
    val neck: Port
)

fun createHead(resolution: Int): MeshNode<HeadPorts> {
  val mesh = FlexibleMesh()
  val headPath = createArc(0.6f, 8, Pi).take(7)
  headPath.forEach { it.x *= 0.8f }
//  headPath.last().x *= 0.5f
  lathe(mesh, headPath, 8 * resolution)
  val edge = mesh.edges.last()
  return MeshNode(mesh, HeadPorts(
      edge
  ), MeshInfo(listOf(), listOf(mapOf(edge to 1f))))
}

data class TorsoPorts(
    val neck: Port
)

fun createTorso(resolution: Int): MeshNode<TorsoPorts> {
  val mesh = FlexibleMesh()

  val neckTop = Vector2(0.06f, 0.9f)
  val bodyFront = convertAsXZ(listOf(
      neckTop,
      Vector2(0.1f, 0.75f),
      Vector2(0.32f, 0.7f),
      Vector2(0.42f, 0.5f),
      Vector2(0.42f, 0f)
  ))

  val bodySide = convertAsXZ(listOf(
      neckTop,
      Vector2(0.1f, 0.75f),
      Vector2(0.2f, 0.5f),
      Vector2(0.25f, 0.4f),
      Vector2(0.25f, 0f)
  ))
  latheTwoPaths(mesh, bodySide, bodyFront, resolution)
  val edge = mesh.edges[0].edges[0].previous!!
  return MeshNode(mesh, TorsoPorts(
      edge
  ), MeshInfo(listOf(), listOf(mapOf(edge to 1f))))
}

fun createHumanMesh(): Pair<FlexibleMesh, MeshInfo> {
  val neck = 0.05f
  val head = createHead(3)
  val torso = createTorso(3)
  val mesh = joinMeshNodes(head.mesh, head.ports.neck, torso.mesh, torso.ports.neck)
  val s = FlexibleMesh()
  createSphere(s, 0.3f, 8, 6)
  distortedTranslatePosition(Vector3(0.1f, 0f, 0f), s)
  mesh.sharedImport(s)
  alignToFloor(mesh.distinctVertices, 0f)
  return Pair(mesh, MeshInfo(listOf(), head.info.edgeGroups.plus(torso.info.edgeGroups)))
}

val createCartoonHuman: ModelGenerator = {
  val mesh = FlexibleMesh()
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

  val sphereMeshes = spheres.map {
    val mesh2 = FlexibleMesh()
    createSphere(mesh2, it.first, 8, 8)
    translateMesh(mesh2, it.second)
    mesh.sharedImport(mesh2)
    Pair(mesh2, it.third)
  }

  transformMesh(mesh, Matrix().scale(1.8f))

  alignToFloor(mesh, 0f)

  val materialMap = sphereMeshes.groupBy { it.second }.map { mapMaterial(it.key, it.value.map { it.first }) }
  Model(
      mesh = mesh,
      materials = materialMap
  )
}

val createHuman: ModelGenerator = {
  val (mesh, info) = createHumanMesh()
  Model(
      mesh = mesh,
      info = info,
      materials = listOf(mapMaterial(Material(Vector4(0.3f, 0.25f, 0.0f, 1f)), mesh)
      ))
}

val createMonster: ModelGenerator = {
  val (mesh, info) = createHumanMesh()
  Model(
      mesh = mesh,
      info = info,
      materials = listOf(mapMaterial(Material(Vector4(0.25f, 0.25f, 0.25f, 1f)), mesh)
      ))
}