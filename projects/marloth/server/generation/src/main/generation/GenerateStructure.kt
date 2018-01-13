package generation

import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.Pi
import mythic.spatial.times
import mythic.spatial.toVector3
import mythic.spatial.Vector3
import org.joml.minus
import org.joml.plus
import org.joml.xy

data class Corner(val position: Vector3, val angle: Float, val isDoorway: Boolean = false)
data class TempSector(val corners: List<Corner>, val nodes: List<Node>)

//fun radialSequence(corners: List<Corner>) =
//    corners.asSequence().plus(Corner(corners.first().position, corners.first().angle + Pi * 2))

fun createCorner(position: Vector3, node: Node, isDoorway: Boolean = false) =
    Corner(position, getAngle(node.position.xy, position.xy), isDoorway)

fun createDoorway(node: Node, other: Node): List<Corner> {
  val direction = (other.position - node.position).xy.normalize()
  val point = node.position.xy + direction * node.radius
  return forkVector(point, direction, 1.5f)
      .map { createCorner(it.toVector3(), node) }
}

fun createVerticesForOverlappingCircles(node: Node, other: Node): List<Corner> =
    circleIntersection(node.position.xy, node.radius, other.position.xy, other.radius)
        .map { createCorner(it.toVector3(), node) }

fun createNodeDoorways(node: Node) =
    node.connections
        .filter { it.type != ConnectionType.union }
        .map { createDoorway(node, it.getOther(node)) }
        .flatten()

data class CornerGap(val first: Corner, val second: Corner, val length: Float)

fun getGaps(corners: List<Corner>, minAngle: Float): List<CornerGap> {
  val result: MutableList<CornerGap> = mutableListOf()
  val first = corners.first()
  var previous = first
  for (next in corners.drop(1)) {
    val length = next.angle - previous.angle
    if (length > minAngle) {
      result.add(CornerGap(previous, next, length))
    }
    previous = next
  }
  val length = first.angle + Pi - previous.angle
  if (length > minAngle) {
    result.add(CornerGap(previous, first, length))
  }
  return result
}

fun fillCornerGaps(unorderedCorners: List<Corner>, node: Node): List<Corner> {
  val corners = unorderedCorners.sortedBy { it.angle }
  val minAngle = Pi * 0.3f
  val gaps = getGaps(corners, minAngle)
  if (gaps.size == 1)
    return corners
  val newCorners = mutableListOf<Corner>()
  for (gap in gaps) {
    val count = (gap.length / minAngle).toInt()
    val increment = gap.length / count
    for (i in 0..count) {
      val angle = gap.first.angle + increment * i
      val position = project2D(angle, node.radius).toVector3() + node.position
      newCorners.add(Corner(position, angle))
    }
  }

  return corners.plus(newCorners).sortedBy { it.angle }
}

fun createSingleNodeStructure(node: Node): TempSector =
    TempSector(
        fillCornerGaps(createNodeDoorways(node), node),
        listOf(node)
    )

fun getClusterUnions(cluster: Cluster): List<Connection> =
    cluster.flatMap { it.connections.filter { it.type == ConnectionType.union } }.distinct()

fun createClusterStructure(cluster: Cluster) =
    TempSector(
        getClusterUnions(cluster)
            .flatMap { createVerticesForOverlappingCircles(it.first, it.second) }
            .plus(cluster.flatMap { createNodeDoorways(it) }),
        cluster
    )

fun skinSector(tempSector: TempSector, mesh: HalfEdgeMesh) {
  for (corner in tempSector.corners) {
    mesh.addVertex(corner.position)
    println(corner.angle)
  }
}

fun generateStructure(abstractWorld: AbstractWorld, structureWorld: StructureWorld) {
  val mesh = structureWorld.mesh

  val singleNodes = abstractWorld.nodes.filter { !isInCluster(it) }
  val clusters = gatherClusters(abstractWorld.nodes)
  val tempSectors = singleNodes.map { createSingleNodeStructure(it) }
      .plus(clusters.map { createClusterStructure(it) })

  tempSectors.forEach { skinSector(it, mesh) }
}