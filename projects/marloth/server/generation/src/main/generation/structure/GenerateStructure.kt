package generation.structure

import generation.*
import generation.abstract.Cluster
import generation.abstract.gatherClusters
import generation.abstract.isInCluster
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.calculateNormals
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.xy
import simulation.*

enum class CornerType {
  doorway,
  normal,
  overlap,
}

val doorwayLength = 2.5f

//data class Corner(val position: Vector3, val type: CornerType = CornerType.normal)
typealias Corner = Vector3

typealias SectorEdge = List<Vector3>

data class TempSector(val node: Node, val corners: List<Corner>)
//data class ConnectionSector(val corners: List<Corner>, val connection: Connection)
data class NodeCorner(val corner: Corner, val angle: Float) {
  val position: Vector3
    get() = corner
}

fun radialSequence(corners: List<NodeCorner>) =
    corners.asSequence().plus(NodeCorner(corners.first().corner, corners.first().angle + Pi * 2))

fun getDoorwayPoints(node: Node, other: Node): List<Vector2> {
  val direction = (other.position - node.position).xy.normalize()
  val point = node.position.xy + direction * node.radius
  return forkVector(point, direction, doorwayLength)
}

fun createDoorway(node: Node, other: Node) =
    getDoorwayPoints(node, other)
        .map { it.toVector3() }

fun createVerticesForOverlappingCircles(node: Node, other: Node, others: List<Node>): List<Corner> =
    circleIntersection(node.position.xy, node.radius, other.position.xy, other.radius)
        // Only needed for tri-unions
//        .filter { c -> !others.any { isInsideNode(c, it) } }
        .map { it.toVector3() }

fun createNodeDoorways(node: Node) =
    node.connections
        .filter { it.type != ConnectionType.union }
        .map { createDoorway(node, it.getOther(node)) }

data class CornerGap(val first: NodeCorner, val second: NodeCorner, val length: Float)

fun mapCorners(corners: List<Corner>, node: Node) =
    corners.map { NodeCorner(it, getAngle(node, it)) }

fun getGaps(corners: List<NodeCorner>, minAngle: Float) =
    toPairs(radialSequence(corners))
        .filter { it.second.angle - it.first.angle > minAngle }
        .map { CornerGap(it.first, it.second, it.second.angle - it.first.angle) }

fun fillCornerGaps(unorderedCorners: List<Corner>, node: Node): List<Corner> {
  val corners = mapCorners(unorderedCorners, node).sortedBy { getAngle(node, it.position) }
  val minAngle = Pi * 0.3f
  val gaps = getGaps(corners, minAngle)
  val newCorners = mutableListOf<Corner>()
  for (gap in gaps) {
    val count = (gap.length / minAngle).toInt()
    val increment = gap.length / (count + 1)
    for (i in 1..count) {
      val angle = gap.first.angle + increment * i
      val position = project2D(angle, node.radius).toVector3() + node.position
      newCorners.add(Corner(position))
    }
  }

  return newCorners
}

fun createSingleNodeStructure(node: Node): TempSector {
  val doorways = createNodeDoorways(node)
  val points = doorways.flatten()
  return TempSector(node, points.plus(fillCornerGaps(points, node)).sortedBy { getAngle(node, it) })
}

fun getClusterUnions(cluster: Cluster): List<Connection> =
    cluster.flatMap { it.connections.filter { it.type == ConnectionType.union } }.distinct()

fun fillClusterCornerGaps(unorderedCorners: List<Corner>, node: Node, otherNodes: List<Node>): List<Corner> {
  val additional = fillCornerGaps(unorderedCorners, node)
  return additional.filter { corner -> !otherNodes.any { isInsideNode(corner.xy, it) } }
}

fun createClusterNodeStructure(node: Node, cluster: List<Node>, corners: List<Corner>): List<Corner> {
  val doorways = createNodeDoorways(node)
  val moreCorners = corners.plus(doorways.flatten())
  return moreCorners
      .plus(fillClusterCornerGaps(moreCorners, node, cluster.filter { it !== node }))
      .sortedBy { getAngle(node, it) }
}

data class SharedCorners(val nodes: List<Node>, val corners: List<Corner>)

fun createClusterStructure(cluster: Cluster): List<TempSector> {
  val overlapPoints = getClusterUnions(cluster)
      .map { i ->
        SharedCorners(listOf(i.first, i.second),
            createVerticesForOverlappingCircles(i.first, i.second, cluster.filter { it !== i.first && it !== i.second }))
      }

  return cluster.map { node ->
    val overlapping = overlapPoints.filter { it.nodes.any { it === node } }.flatMap { it.corners }
    TempSector(node, createClusterNodeStructure(node, cluster, overlapping))
  }
}

fun generateTunnelStructure(node: Node, nodeSectors: List<TempSector>): TempSector {
  val (first, second) = node.getNeighbors().toList()
  val sectors = nodeSectors.filter { it.node === first || it.node == second }
  val corners = sectors.flatMap { sector ->
    val points = getDoorwayPoints(sector.node, if (sector.node === first) second else first)
    sector.corners.filter { p -> points.any { roughlyEquals(it, p.xy, 0.1f) } }
  }
      .sortedBy { getAngle(node, it) }

  return TempSector(node, corners)
}

fun sinewSector(corners: List<Corner>, vertices: Map<Corner, Vector3>, mesh: FlexibleMesh): FlexibleFace {
  val sectorVertices = corners
      .map { vertices[it]!! }
  val face = mesh.createStitchedFace(sectorVertices)
  return face
}

data class Floor(
    val sector: TempSector,
    val face: FlexibleFace
)

fun sinewFloors(nodeSectors: List<TempSector>, mesh: FlexibleMesh):
    List<Floor> {
  val vertices = nodeSectors.flatMap { it.corners }
      .distinct()
      .associate { Pair(it, Vector3(it)) }

//  vertices.values.forEach { mesh.addVertex(it) }

  return nodeSectors.map { sector ->
    val face = sinewSector(sector.corners, vertices, mesh)
    sector.node.floors.add(face)
    Floor(sector, face)
  }
}

fun createWall(edge: FlexibleEdge, mesh: FlexibleMesh): FlexibleFace {
  val offset = Vector3(0f, 0f, 2f)
  return mesh.createStitchedFace(listOf(
      edge.second,
      edge.first,
      edge.first + offset,
      edge.second + offset
  ))
}

//fun getWallEdges(face: FlexibleFace, corners: List<Corner>): List<FlexibleEdge> {
////  fun getCorner(vertex: Vector3) = corners.first { it.position == vertex.position }
//  return face.edges.filter {
//    it.faces.size == 1
//  }
//}

fun <T> crossMap(firstList: List<T>, secondList: List<T>): List<Pair<T, T>> {
  var skip = 1
  val result: MutableList<Pair<T, T>> = mutableListOf()
  for (a in firstList) {
    for (i in skip until secondList.size) {
      val b = secondList[i]
      result.add(Pair(a, b))
    }
    ++skip
  }
  return result
}

fun generateStructure(abstractWorld: AbstractWorld) {
  val mesh = abstractWorld.mesh

  val roomNodes = abstractWorld.nodes.filter { it.type == NodeType.room }

  val singleNodes = roomNodes.filter { !isInCluster(it) }
  val clusters = gatherClusters(roomNodes)
  val nodeSectors = singleNodes.map { createSingleNodeStructure(it) }
      .plus(clusters.flatMap { createClusterStructure(it) })

  val tunnelSectors = abstractWorld.nodes.filter { it.type == NodeType.tunnel }
      .map { generateTunnelStructure(it, nodeSectors) }

  val allSectors = nodeSectors.plus(tunnelSectors)
  sinewFloors(allSectors, mesh)
  allSectors.forEach { sector ->
    val wallBases = sector.node.floors.first().edges.filter { it.edges.size == 0 }
    wallBases.forEach {
      val wall = createWall(it, mesh)
      sector.node.walls.add(wall)
    }
  }

  calculateNormals(mesh)

  initializeFaceInfo(abstractWorld)

  defineNegativeSpace(abstractWorld)
//  val allFloors = roomFloors.map { it.face }//.plus(tunnelFloors.map { it.face })
}