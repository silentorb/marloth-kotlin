package mythic.sculpting

import mythic.sculpting.query.getCenter

typealias Port = FlexibleEdge

//interface PortId {
//  val name: String
//}

//typealias Ports = Map<PortId, Port>

//data class MeshNode(
//    val mesh: FlexibleMesh,
//    val ports: Ports
//)

data class MeshNode<Ports>(
    val mesh: FlexibleMesh,
    val ports: Ports
)

fun joinMeshNodes(first: FlexibleMesh, firstPort: Port, second: FlexibleMesh, secondPort: Port): FlexibleMesh {
//  assert(firstPort.size == secondPort.size)
  val mesh = FlexibleMesh()
  setAnchor(firstPort.middle, first.distinctVertices)
  setAnchor(secondPort.middle, second.distinctVertices)
  mesh.sharedImport(first)
  mesh.sharedImport(second)
  firstPort.first.x += 1f
  secondPort.first.x += 0.2f
  secondPort.first.z += 0.2f
//  val firstMiddles = firstPort.map { Pair(it.middle, it) }
//  val secondMiddles = firstPort.map { Pair(it.middle, it) }

  return mesh
}