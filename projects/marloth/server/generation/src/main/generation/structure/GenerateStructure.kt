package generation.structure

import generation.*
import generation.abstract.*
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.Vertex
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.xy

enum class CornerType {
  doorway,
  normal,
  overlap,
}

data class Corner(val position: Vector3, val type: CornerType = CornerType.normal)
data class NodeSector(val corners: List<Corner>, val node: Node)
data class ConnectionSector(val corners: List<Corner>, val connection: Connection)
data class NodeCorner(val corner: Corner, val angle: Float) {
  val position: Vector3
    get() = corner.position
}

fun radialSequence(corners: List<NodeCorner>) =
    corners.asSequence().plus(NodeCorner(corners.first().corner, corners.first().angle + Pi * 2))

//fun createCorner(position: Vector3, type: CornerType = CornerType.normal) =
//    Corner(position, type)

//fun createCorner(position: Vector3, node: Node, type: CornerType = CornerType.normal) =
//    Corner(position, getAngle(node.position.xy, position.xy), type)

fun getDoorwayPoints(node: Node, other: Node): List<Vector2> {
  val direction = (other.position - node.position).xy.normalize()
  val point = node.position.xy + direction * node.radius
  return forkVector(point, direction, 1.5f)
}

fun createDoorway(node: Node, other: Node) =
    getDoorwayPoints(node, other)
        .map { Corner(it.toVector3(), CornerType.doorway) }

fun createVerticesForOverlappingCircles(node: Node, other: Node, others: List<Node>): List<Corner> =
    circleIntersection(node.position.xy, node.radius, other.position.xy, other.radius)
        .filter { c -> !others.any { isInsideNode(c, it) } }
        .map { Corner(it.toVector3(), CornerType.overlap) }

fun createNodeDoorways(node: Node) =
    node.connections
        .filter { it.type != ConnectionType.union }
        .map { createDoorway(node, it.getOther(node)) }
        .flatten()

data class CornerGap(val first: NodeCorner, val second: NodeCorner, val length: Float)

fun mapCorners(corners: List<Corner>, node: Node) =
    corners.map { NodeCorner(it, getAngle(node, it.position)) }

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
  val corners = createNodeDoorways(node)
  return NodeSector(
      corners.plus(fillCornerGaps(corners, node)).sortedBy { getAngle(node, it.position) },
      node
  )
}

fun getClusterUnions(cluster: Cluster): List<Connection> =
    cluster.flatMap { it.connections.filter { it.type == ConnectionType.union } }.distinct()

fun fillClusterCornerGaps(unorderedCorners: List<Corner>, node: Node, otherNodes: List<Node>): List<Corner> {
  val additional = fillCornerGaps(unorderedCorners, node)
  return additional.filter { corner -> !otherNodes.any { isInsideNode(corner.position.xy, it) } }
}

fun createClusterNodeStructure(node: Node, cluster: List<Node>, corners: List<Corner>): List<Corner> {
  val moreCorners = corners.plus(createNodeDoorways(node))
  return moreCorners
      .plus(fillClusterCornerGaps(moreCorners, node, cluster.filter { it !== node }))
}

data class SharedCorners(val nodes: List<Node>, val corners: List<Corner>)

fun createClusterStructure(cluster: Cluster): List<NodeSector> {
  val overlapPoints = getClusterUnions(cluster)
      .map { i ->
        SharedCorners(listOf(i.first, i.second),
            createVerticesForOverlappingCircles(i.first, i.second, cluster.filter { it !== i.first && it !== i.second }))
      }
//  return NodeSector(
//      overlapPoints.map { it.corners }.flatten()
//          .plus(cluster.flatMap { node ->
//            createClusterNodeStructure(node, cluster,
//                overlapPoints.filter { it.nodes.any { it === node } }.flatMap { it.corners }
//            )
//          }),
//      cluster
//  )

  return cluster.map { node ->
    val overlapping = overlapPoints.filter { it.nodes.any { it === node } }.flatMap { it.corners }
    NodeSector(createClusterNodeStructure(node, cluster, overlapping), node)
  }
}

fun generateTunnelStructure(connection: Connection, nodeSectors: List<NodeSector>): ConnectionSector {
  val sectors = nodeSectors.filter { it.node === connection.first || it.node == connection.second }
  val corners = sectors.flatMap { sector ->
    val points = getDoorwayPoints(sector.node, connection.getOther(sector.node))
    sector.corners.filter { p -> points.any { it == p.position.xy } }
  }

  return ConnectionSector(corners, connection)
}

fun sinewSector(corners: List<Corner>, center: Vector2, vertices: Map<Corner, Vertex>, mesh: HalfEdgeMesh) {
  val face = mesh.createFace()
  val sectorVertices = corners
      .sortedBy { getAngle(center, it.position.xy) }
      .map { vertices[it]!! }
  mesh.replaceFaceVertices(face, sectorVertices)
}

fun sinew(nodeSectors: List<NodeSector>, tunnelSectors: List<ConnectionSector>, mesh: HalfEdgeMesh) {
  val vertices = nodeSectors.flatMap { it.corners }
      .distinct()
      .associate { Pair(it, Vertex(it.position)) }

  vertices.values.forEach { mesh.addVertex(it) }
  nodeSectors.forEach { sector ->
    sinewSector(sector.corners, sector.node.position.xy, vertices, mesh)
//    val face = mesh.createFace()
//    val sectorVertices = sector.corners
//        .sortedBy { getAngle(sector.node, it.position) }
//        .map { vertices[it]!! }
//    mesh.replaceFaceVertices(face, sectorVertices)
  }

  tunnelSectors.forEach { sector ->
    val center = getCenter(sector.corners.map { it.position.xy })
    sinewSector(sector.corners, center, vertices, mesh)
  }
}

fun generateStructure(abstractWorld: AbstractWorld, structureWorld: StructureWorld) {
  val mesh = structureWorld.mesh

  val singleNodes = abstractWorld.nodes.filter { !isInCluster(it) }
  val clusters = gatherClusters(abstractWorld.nodes)
  val nodeSectors = singleNodes.map { createSingleNodeStructure(it) }
      .plus(clusters.flatMap { createClusterStructure(it) })

  val tunnelSectors = abstractWorld.connections.filter { it.type == ConnectionType.tunnel }
      .map { generateTunnelStructure(it, nodeSectors) }

  sinew(nodeSectors, tunnelSectors, mesh)
}