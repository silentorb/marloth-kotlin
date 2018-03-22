package mythic.sculpting

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
    val ports: Ports,
    val info: MeshInfo
)

fun joinMeshNodes(first: FlexibleMesh, firstPort: Port, second: FlexibleMesh, secondPort: Port): FlexibleMesh {
//  assert(firstPort.size == secondPort.size)
  val mesh = FlexibleMesh()
  val firstLoop = getEdgeLoop(firstPort)
  val secondLoop = getEdgeLoopReversed(secondPort)
  setAnchor(getCenter(firstLoop), first.distinctVertices)
  setAnchor(getCenter(secondLoop), second.distinctVertices)
  mesh.sharedImport(first)
  mesh.sharedImport(second)

  stitchEdgeLoops(firstLoop, secondLoop)

  return mesh
}