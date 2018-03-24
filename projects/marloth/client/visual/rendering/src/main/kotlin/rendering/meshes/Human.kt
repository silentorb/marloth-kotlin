package rendering.meshes

import mythic.sculpting.*
import mythic.spatial.Pi
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import rendering.Material
import rendering.MeshElement
import rendering.Model

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
  ), MeshInfo(listOf(), listOf(mapOf(edge to 1f)), listOf()))
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
  latheTwoPaths(mesh, bodyFront, bodySide, resolution)
  val edge = mesh.edges[0].edges[0].previous!!
  return MeshNode(mesh, TorsoPorts(
      edge
  ), MeshInfo(listOf(), listOf(mapOf(edge to 1f)), listOf()))
}

fun createHuman(): Model {
  val neck = 0.05f
  val head = createHead(3)
  val torso = createTorso(3)
  val mesh = joinMeshNodes(head.mesh, head.ports.neck, torso.mesh, torso.ports.neck)
  alignToFloor(mesh.distinctVertices, 0f)
  return Model(
      mesh = mesh,
      info = MeshInfo(listOf(), head.info.edgeGroups.plus(torso.info.edgeGroups), listOf()),
      defaultMaterial = Material(Vector4(0.3f, 0.25f, 0.0f, 1f))
  )
}

fun createMonster(): Model {
  val neck = 0.05f
  val head = createHead(1)
  val torso = createTorso(1)
//  val mesh = joinMeshNodes(head.mesh, head.ports.neck, torso.mesh, torso.ports.neck)
  val mesh = joinMeshNodes(torso.mesh, torso.ports.neck, head.mesh, head.ports.neck)
  alignToFloor(mesh.distinctVertices, 0f)
  return Model(
      mesh = mesh,
      info = MeshInfo(listOf(), head.info.edgeGroups.plus(torso.info.edgeGroups), listOf()),
      defaultMaterial = Material(Vector4(0.25f, 0.25f, 0.25f, 1f))
  )
}