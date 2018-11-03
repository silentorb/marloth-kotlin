package generation.structure

import generation.abstract.OldRealm
import mythic.ent.Id
import mythic.sculpting.*
import mythic.spatial.*
import physics.voidNodeId
import simulation.FaceType
import mythic.ent.IdSource
import mythic.ent.entityMap
import simulation.newIdSource
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

fun createSecondaryNode(sectorCenter: Vector3, nextId: IdSource, isSolid: Boolean): Node {
  val radius = 1f

  val node = Node(
      id = nextId(),
      position = sectorCenter,
      radius = radius,
      isSolid = isSolid,
      isWalkable = false,
      biome = Biome.void,
      height = 0f,
      floors = mutableListOf(),
      ceilings = mutableListOf(),
      walls = mutableListOf()
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


fun createSurface(mesh: ImmutableMesh, id: Id, node: Id, vertices: Vertices, faceType: FaceType): FacePair {
  val floor = mesh.createStitchedFace(id, vertices)
  val info = ConnectionFace(id, faceType, node, voidNodeId)
  return FacePair(info, floor)
}

fun createFloor(mesh: ImmutableMesh, nextId: IdSource, node: Node, vertices: Vertices, center: Vector2): FacePair {
  val sortedFloorVertices = vertices
      .sortedBy { atan(it.xy() - center) }

  val result = createSurface(mesh, nextId(), node.id, sortedFloorVertices, FaceType.floor)
  node.floors.add(result.geometry)
  return result
}

fun createCeiling(mesh: ImmutableMesh, nextId: IdSource, node: Node, vertices: Vertices, center: Vector2): FacePair {
  val sortedFloorVertices = vertices
      .sortedByDescending { atan(it.xy() - center) }
      .map { it + Vector3(0f, 0f, wallHeight) }

  val result = createSurface(mesh, nextId(), node.id, sortedFloorVertices, FaceType.ceiling)
  node.ceilings.add(result.geometry)
  return result
}

fun createWall(mesh: ImmutableMesh, nextId: IdSource, node: Node, vertices: Vertices): FacePair {
  val result = createSurface(mesh, nextId(), node.id, vertices, FaceType.wall)
  node.walls.add(result.geometry)
  return result
}

fun verticalEdges(face: ImmutableFace) =
    face.edges.asSequence().filter { it.first.x == it.second.x && it.first.y == it.second.y }

fun idSourceFromNodes(nodes: List<Node>): IdSource =
    newIdSource(if (nodes.any())
      nodes.sortedByDescending { it.id }.first().id + 1L
    else
      1L
    )