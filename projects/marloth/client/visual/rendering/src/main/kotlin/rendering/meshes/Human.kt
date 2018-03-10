package rendering.meshes

import mythic.sculpting.*
import mythic.spatial.Pi
import mythic.spatial.Vector2

data class HeadPorts(
    val neck: Port
)

fun createHead(): MeshNode<HeadPorts> {
  val mesh = FlexibleMesh()
  val headPath = createArc(0.6f, 8, Pi).take(7)
  headPath.last().x *= 0.5f
  lathe(mesh, headPath, 8 * 3)
  return MeshNode(mesh, HeadPorts(
      edgeLoopNext(mesh.edges.last())
  ))
}

data class TorsoPorts(
    val neck: Port
)

fun createTorso(): MeshNode<TorsoPorts> {
  val mesh = FlexibleMesh()

  val bodyFront = convertAsXZ(listOf(
      Vector2(0.05f, 1f),
      Vector2(0.1f, 0.75f),
      Vector2(0.4f, 0.7f),
      Vector2(0.5f, 0.25f),
      Vector2(0.5f, 0f)
  ))

  val bodySide = convertAsXZ(listOf(
      Vector2(0.05f, 1f),
      Vector2(0.1f, 0.75f),
      Vector2(0.2f, 0.5f),
      Vector2(0.25f, 0.25f),
      Vector2(0.25f, 0f)
  ))
  latheTwoPaths(mesh, bodyFront, bodySide)
  return MeshNode(mesh, TorsoPorts(
      mesh.edges.first()
  ))
}

fun createHuman(): FlexibleMesh {
  val neck = 0.05f
  val head = createHead()
  val torso = createTorso()
  val mesh = joinMeshNodes(head.mesh, head.ports.neck, torso.mesh, torso.ports.neck)
//  val mesh = FlexibleMesh()
//  setAnchor(head.ports.neck.middle, head.mesh.distinctVertices)
//  mesh.sharedImport(head.mesh)
//  val mesh = head.mesh
//  alignToFloor(mesh.distinctVertices, 1f)
  return mesh
}