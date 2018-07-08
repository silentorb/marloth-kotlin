package generation.structure

import mythic.sculpting.Edges
import mythic.sculpting.FlexibleFace
import mythic.sculpting.Vertices
import mythic.sculpting.getNormal
import mythic.spatial.Vector3
import mythic.spatial.arrangePointsCounterClockwise
import mythic.spatial.arrangePointsCounterClockwise2D
import mythic.spatial.getCenter
import org.joml.minus
import org.joml.plus
import simulation.AbstractWorld
import simulation.Node

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

fun createSecondaryNode(sectorCenter: Vector3, abstractWorld: AbstractWorld, isSolid: Boolean): Node {
  val radius = 1f

  val node = Node(
      position = sectorCenter,
      radius = radius,
      isSolid = isSolid
  )
  node.index = abstractWorld.graph.nodes.size
  return node
}