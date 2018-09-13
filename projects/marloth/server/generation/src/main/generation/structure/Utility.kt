package generation.structure

import generation.abstract.FaceInfo
import generation.abstract.Graph
import generation.abstract.Node
import generation.abstract.Realm
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import simulation.FaceType
import simulation.IdSource
import simulation.newIdSource

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

fun getWallVertices(face: FlexibleFace): WallVertices =
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
  val center = Vector3(getCenter(vertices))
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
  val center = Vector3(getCenter(sorted))
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
      isWalkable = false
  )
  return node
}

fun createFloor(mesh: FlexibleMesh, node: Node, vertices: Vertices, center: Vector2): FlexibleFace {
  val sortedFloorVertices = vertices
      .sortedBy { atan(it.xy() - center) }
  val floor = mesh.createStitchedFace(sortedFloorVertices)
  floor.data = FaceInfo(FaceType.floor, node, null)
  node.floors.add(floor)
  return floor
}

fun createCeiling(mesh: FlexibleMesh, node: Node, vertices: Vertices, center: Vector2): FlexibleFace {
  val sortedFloorVertices = vertices
      .sortedByDescending { atan(it.xy() - center) }
      .map { it + Vector3m(0f, 0f, wallHeight) }

  val surface = mesh.createStitchedFace(sortedFloorVertices)
  node.ceilings.add(surface)
  surface.data = FaceInfo(FaceType.ceiling, node, null)
  return surface
}

fun createWall(realm: Realm, node: Node, vertices: Vertices): FlexibleFace {
  val wall = realm.mesh.createStitchedFace(vertices)
  wall.data = FaceInfo(FaceType.wall, node, null)
  node.walls.add(wall)
  return wall
}

fun verticalEdges(face: FlexibleFace) =
    face.edges.asSequence().filter { it.first.x == it.second.x && it.first.y == it.second.y }

fun idSourceFromNodes(nodes: List<Node>): IdSource =
    newIdSource(if (nodes.any())
      nodes.sortedByDescending { it.id }.first().id + 1L
    else
      1L
    )