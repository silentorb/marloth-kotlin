package generation.structure

import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
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

fun getWallVertices(face: FlexibleFace): WallVertices =
    getWallVertices(face.vertices)


fun getNewWallVertices(sectorCenter: Vector3, edges: Edges): Vertices {
  val sortedEdges = edges.map { it.vertices.sortedBy { it.z } }
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

fun createSecondaryNode(sectorCenter: Vector3, abstractWorld: AbstractWorld, isSolid: Boolean, biome: Biome): Node {
  val radius = 1f

  val node = Node(
      position = sectorCenter,
      radius = radius,
      biome = biome,
      isSolid = isSolid
  )
  node.index = abstractWorld.graph.nodes.size
  return node
}

fun createFloor(mesh: FlexibleMesh, node: Node, vertices: Vertices, center: Vector2): FlexibleFace {
  val sortedFloorVertices = vertices
      .sortedBy { atan(it.xy - center) }
  val floor = mesh.createStitchedFace(sortedFloorVertices)
  floor.data = FaceInfo(FaceType.floor, node, null)
  node.floors.add(floor)
  return floor
}

fun createCeiling(mesh: FlexibleMesh, node: Node, vertices: Vertices, center: Vector2): FlexibleFace {
  val sortedFloorVertices = vertices
      .sortedByDescending { atan(it.xy - center) }
      .map { it + Vector3(0f, 0f, wallHeight) }

  val surface = mesh.createStitchedFace(sortedFloorVertices)
  node.ceilings.add(surface)
  surface.data = FaceInfo(FaceType.ceiling, node, null)
  return surface
}

fun createWall(abstractWorld: AbstractWorld, node: Node, vertices: Vertices): FlexibleFace {
  val wall = abstractWorld.mesh.createStitchedFace(vertices)
  wall.data = FaceInfo(FaceType.wall, node, null)
  node.walls.add(wall)
  return wall
}
