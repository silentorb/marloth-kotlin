package generation.structure

import mythic.ent.Id
import mythic.sculpting.*
import mythic.spatial.*
import physics.voidNodeId
import simulation.FaceType
import mythic.ent.IdSource
import mythic.ent.entityMap
import mythic.ent.newIdSource
import simulation.*

data class WallVertices(
    val lower: Vertices,
    val upper: Vertices
)

fun getWallVertices(vertices: Vertices): WallVertices {
  val sortedOriginalPoints = vertices.sortedBy { it.z }
  return WallVertices(sortedOriginalPoints.take(2),
      sortedOriginalPoints.drop(2)
  )
}

fun getWallVertices(face: ImmutableFace): WallVertices =
    getWallVertices(face.vertices)


fun getNewWallVertices(sectorCenter: Vector3, edges: Edges): Vertices {
  val sortedEdges = edges.map { edge -> edge.vertices.sortedBy { it.z } }
  val vertices = listOf(
      sortedEdges[0][0],
      sortedEdges[1][0],
      sortedEdges[1][1],
      sortedEdges[0][1]
  )
  val normal = getNormal(vertices)
  val center = getCenter(vertices)
  if (sectorCenter.distance(center + normal) > sectorCenter.distance(center - normal)) {
    return listOf(
        sortedEdges[1][0],
        sortedEdges[0][0],
        sortedEdges[0][1],
        sortedEdges[1][1]
    )
  }

  return vertices
}

fun sortWallVertices(sectorCenter: Vector3, vertices: Vertices): Vertices {
  val sorted = arrangePointsCounterClockwise(vertices)
  val normal = getNormal(sorted)
  val center = getCenter(sorted)
  return if (sectorCenter.distance(center + normal) > sectorCenter.distance(center - normal))
    sorted.reversed()
  else
    sorted
}

fun createSecondaryNode(sectorCenter: Vector3, nextId: IdSource, isSolid: Boolean, biome: Biome): Node {
  val radius = 1f

  val node = Node(
      id = nextId(),
      position = sectorCenter,
      radius = radius,
      isSolid = isSolid,
      isWalkable = false,
      biome = biome,
      height = 0f
  )
  return node
}

data class FacePair(
    val info: ConnectionFace,
    val geometry: ImmutableFace
)

fun splitFacePairs(pairs: List<FacePair>): Pair<List<ConnectionFace>, List<ImmutableFace>> =
    Pair(
        pairs.map { it.info },
        pairs.map { it.geometry }
    )

fun splitFacePairTables(pairs: List<FacePair>): Pair<ConnectionTable, ImmutableFaceTable> =
    Pair(
        entityMap(pairs.map { it.info }),
        entityMap(pairs.map { it.geometry })
    )


fun createSurface(nextEdgeId: IdSource, mesh: ImmutableMesh, id: Id, node: Id, vertices: Vertices, faceType: FaceType): FacePair {
  val floor = mesh.createStitchedFace(nextEdgeId, id, vertices)
  val info = ConnectionFace(id, faceType, node, voidNodeId)
  return FacePair(info, floor)
}

fun createFloor(idSources: GeometryIdSources, mesh: ImmutableMesh, node: Node, vertices: Vertices, center: Vector2): FacePair {
  val sortedFloorVertices = vertices
      .sortedBy { atan(it.xy() - center) }

  val result = createSurface(idSources.edge, mesh, idSources.face(), node.id, sortedFloorVertices, FaceType.floor)
  node.floors.add(result.geometry.id)
  return result
}

fun createCeiling(idSources: GeometryIdSources, mesh: ImmutableMesh, node: Node, vertices: Vertices, center: Vector2): FacePair {
  val sortedFloorVertices = vertices
      .sortedByDescending { atan(it.xy() - center) }
//      .map { it + Vector3(0f, 0f, wallHeight) }

  val result = createSurface(idSources.edge, mesh, idSources.face(), node.id, sortedFloorVertices, FaceType.ceiling)
  node.ceilings.add(result.geometry.id)
  return result
}

fun createWall(idSources: GeometryIdSources, mesh: ImmutableMesh, node: Node, vertices: Vertices): FacePair {
  val result = createSurface(idSources.edge, mesh, idSources.face(), node.id, vertices, FaceType.wall)
  node.walls.add(result.geometry.id)
  return result
}

fun verticalEdges(face: ImmutableFace) =
    face.edges.asSequence().filter { it.first.x == it.second.x && it.first.y == it.second.y }

fun idSourceFromNodes(nodes: Collection<Node>): IdSource =
    newIdSource(if (nodes.any())
      nodes.sortedByDescending { it.id }.first().id + 1L
    else
      1L
    )

fun zeroIfVoid(value: Id) =
    if (value == voidNodeId)
      0
    else
      1

fun faceNodeCount(faceInfo: ConnectionFace) =
    zeroIfVoid(faceInfo.firstNode) + zeroIfVoid(faceInfo.secondNode)

fun faceNodeCount(faces: ConnectionTable, face: ImmutableFace) =
    faceNodeCount(faces[face.id]!!)

fun getSharedEdge(first: ImmutableFace, second: ImmutableFace): ImmutableEdge =
    first.edges.first { edge -> second.edges.map { it.edge }.contains(edge.edge) }.edge

// This algorithm only works on quads
fun getOppositeQuadEdge(first: ImmutableFace, edge: ImmutableEdge) =
    first.edges.first { e -> e.vertices.none { edge.vertices.contains(it) } }

fun edgeDot(firstVertices: List<Vector3>, secondVertices: List<Vector3>, secondNormal: Vector3): Float {
  val middle = firstVertices.first { secondVertices.contains(it) }
  val firstOuterEdge = firstVertices.first { it != middle }
  val firstVector = (firstOuterEdge - middle).normalize()
  return firstVector.dot(secondNormal)
}

fun edgeDot(first: ImmutableFace, second: ImmutableFace): Float {
  val sharedEdge = getSharedEdge(first, second)
  val middle = sharedEdge.middle
  val firstOuterEdge = getOppositeQuadEdge(first, sharedEdge)
  val firstVector = (firstOuterEdge.middle - middle).normalize()
  return firstVector.dot(second.normal)
}

fun isConcaveCorner(first: ImmutableFace, second: ImmutableFace): Boolean =
    edgeDot(first, second) > 0f

fun isConcaveCorner(a: Vector3, b: Vector3, bcNormal: Vector3): Boolean {
  val firstVector = (a - b).normalize()
  return firstVector.dot(bcNormal) > 0
}

//    val neighborLists = remaining.map { wall -> Pair(wall, getIncompleteNeighbors(currentRealm.connections, wall).toList()) }
//    val invalid = neighborLists.filter { it.second.size > 2 }
////    assert(invalid.none())
//    if (invalid.any()) {
//      val temp = invalid.map { (a, b) ->
//        realm.connections[a.id]!!.debugInfo = "space-a"
//        Pair(realm.connections[a.id]!!, b.map { realm.connections[it.id]!! })
//      }
//      val nd = temp.flatMap { (a, b) -> b.map { it.firstNode }.plus(a.firstNode) }
//          .distinct()
//      return currentRealm
//    }

//    assert(invalid.none())
