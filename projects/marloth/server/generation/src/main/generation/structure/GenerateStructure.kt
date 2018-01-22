package generation.structure

import generation.*
import generation.abstract.Cluster
import generation.abstract.gatherClusters
import generation.abstract.isInCluster
import mythic.sculpting.HalfEdge
import mythic.sculpting.HalfEdgeFace
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.HalfEdgeVertex
import mythic.sculpting.query.getEdges
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

//data class Corner(val position: Vector3, val type: CornerType = CornerType.normal)
typealias Corner = Vector3

typealias SectorEdge = List<Vector3>

data class NodeSector(val node: Node, val corners: List<Corner>)
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
  return forkVector(point, direction, 1.5f)
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
    for (i in 0..count) {
      val angle = gap.first.angle + increment * i
      val position = project2D(angle, node.radius).toVector3() + node.position
      newCorners.add(Corner(position))
    }
  }

  return newCorners
}

fun createSingleNodeStructure(node: Node): NodeSector {
  val doorways = createNodeDoorways(node)
  val points = doorways.flatten()
  return NodeSector(
      node,
      points.plus(fillCornerGaps(points, node)).sortedBy { getAngle(node, it) }
//      doorways
  )
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

fun createClusterStructure(cluster: Cluster): List<NodeSector> {
  val overlapPoints = getClusterUnions(cluster)
      .map { i ->
        SharedCorners(listOf(i.first, i.second),
            createVerticesForOverlappingCircles(i.first, i.second, cluster.filter { it !== i.first && it !== i.second }))
      }

  return cluster.map { node ->
    val overlapping = overlapPoints.filter { it.nodes.any { it === node } }.flatMap { it.corners }
    NodeSector(node, createClusterNodeStructure(node, cluster, overlapping))
  }
}

//fun generateTunnelStructure(connection: Connection, nodeSectors: List<NodeSector>): ConnectionSector {
//  val sectors = nodeSectors.filter { it.node === connection.first || it.node == connection.second }
//  val corners = sectors.flatMap { sector ->
//    val points = getDoorwayPoints(sector.node, connection.getOther(sector.node))
//    sector.corners.filter { p -> points.any { it == p.xy } }
//  }
//
//  return ConnectionSector(corners, connection)
//}

fun sinewSector(corners: List<Corner>, vertices: Map<Corner, HalfEdgeVertex>, mesh: HalfEdgeMesh): HalfEdgeFace {
  val sectorVertices = corners
      .map { vertices[it]!! }
  val face = mesh.createFace(sectorVertices)
  return face
}

data class Floor(
    val sector: NodeSector,
    val face: HalfEdgeFace
)

//data class TunnelFloor(
//    val sector: ConnectionSector,
//    val face: HalfEdgeFace
//)

fun sinewFloors(nodeSectors: List<NodeSector>, mesh: HalfEdgeMesh):
    List<Floor> {
  val vertices = nodeSectors.flatMap { it.corners }
      .distinct()
      .associate { Pair(it, HalfEdgeVertex(it)) }

  val v = vertices.values.distinctBy { it.position }
  vertices.values.forEach { mesh.addVertex(it) }

  return nodeSectors.map { sector ->
    Floor(sector, sinewSector(sector.corners, vertices, mesh))
  }
//  ,
//      tunnelSectors.map { sector ->
//        val center = getCenter(sector.corners.map { it.xy })
//        TunnelFloor(sector, sinewSector(sector.corners, vertices, mesh))
//      }
//  )
}

fun createWall(edge: HalfEdge, mesh: HalfEdgeMesh): HalfEdgeFace {
  val offset = Vector3(0f, 0f, 1f)
  val next = edge.next!!.vertex
  return mesh.createFace(listOf(
      edge.vertex,
      next,
      mesh.addVertex(next.position + offset),
      mesh.addVertex(edge.vertex.position + offset)
  ))
}

fun getWallEdges(face: HalfEdgeFace, corners: List<Corner>): List<HalfEdge> {
  val edges = getEdges(face)
//  fun getCorner(vertex: HalfEdgeVertex) = corners.first { it.position == vertex.position }
  return edges.filter {
    it.opposite == null
//    val first = getCorner(it.vertex).type
//    val second = getCorner(it.next!!.vertex).type
//    first == CornerType.normal || second != first
  }
}

//fun <T, O> crossMap(firstList: List<T>, secondList: List<T>, action: (T, T) -> O?): List<O> {
//  var skip = 1
//  val result: MutableList<O> = mutableListOf()
//  for (a in firstList) {
//    for (i in skip until secondList.size) {
//      val b = secondList[i]
//      val output = action(a, b)
//      if (output != null)
//        result.add(output)
//    }
//    ++skip
//  }
//  return result
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

//data class ClosePointPair(
//    var first: Pair<NodeSector, Vector3>,
//)

//fun findClosePoints(sectors: List<List<Corner>>):List<ClosePointPair > {
//  return crossMap(sectors, sectors)
//      .map { first, second ->
//        crossMap(first, second)
////            .filter { a, b -> a.distance(b) < 0.5f }
//      }
//      .flatten()
//}

fun generateStructure(abstractWorld: AbstractWorld, structureWorld: StructureWorld): MeshGroups {
  val mesh = structureWorld.mesh

  val singleNodes = abstractWorld.nodes.filter { !isInCluster(it) }
  val clusters = gatherClusters(abstractWorld.nodes)
  val nodeSectors = singleNodes.map { createSingleNodeStructure(it) }
      .plus(clusters.flatMap { createClusterStructure(it) })

//  val tunnelSectors = abstractWorld.connections.filter { it.type == ConnectionType.tunnel }
//      .map { generateTunnelStructure(it, nodeSectors) }

  val roomFloors = sinewFloors(nodeSectors, mesh)
  val walls = roomFloors.flatMap { getWallEdges(it.face, it.sector.corners) }
//      .plus(tunnelFloors.flatMap { getWallEdges(it.face, it.sector.corners) })
      .map { createWall(it, mesh) }
  val allFloors = roomFloors.map { it.face }//.plus(tunnelFloors.map { it.face })
  return MeshGroups(allFloors, walls)
}