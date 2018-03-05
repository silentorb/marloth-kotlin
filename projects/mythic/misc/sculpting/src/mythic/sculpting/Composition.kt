package mythic.sculpting

import mythic.sculpting.query.getCenter

typealias Port = Edges

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
  assert(firstPort.size == secondPort.size)
  val mesh = FlexibleMesh()
  setAnchor(getCenter(firstPort.map { it.first }), first.distinctVertices)
  setAnchor(getCenter(secondPort.map { it.first }), second.distinctVertices)
  mesh.sharedImport(first)
  mesh.sharedImport(second)
  val firstMiddles = firstPort.map { Pair(it.middle, it) }
  val secondMiddles = firstPort.map { Pair(it.middle, it) }

  return mesh
}