package generation

import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.times
import mythic.spatial.toVector3
import mythic.spatial.Vector3
import org.joml.minus
import org.joml.plus
import org.joml.xy

data class Corner(val position: Vector3, val angle: Float, val isDoorway: Boolean = false)
data class TempSector(val corners: List<Corner>, val nodes: List<Node>)

val isInCluster = { node: Node ->
  node.connections.any { it.type == ConnectionType.union }
}

typealias Cluster = MutableList<Node>
typealias Clusters = MutableList<Cluster>

fun createCluster(clusters: Clusters): Cluster {
  val cluster = mutableListOf<Node>()
  clusters.add(cluster)
  return cluster
}

fun gatherClusters(allNodes: List<Node>): Clusters {
  val nodes = allNodes.filter(isInCluster)
  val clusters = mutableListOf<Cluster>()

  fun getCluster(node: Node) = clusters.filter { it.any { it === node } }
      .firstOrNull()

  for (node in nodes) {
    val cluster = getCluster(node) ?: createCluster(clusters)
    for (connection in node.connections.filter { it.type == ConnectionType.union }) {
      val other = connection.getOther(node)
      val otherCluster = getCluster(other)
      if (otherCluster == null) {
        cluster.add(other)
      } else if (otherCluster !== cluster) {
        cluster.plusAssign(otherCluster)
        clusters.remove(otherCluster)
      }
    }
  }
  return clusters
}

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

fun createSingleNodeStructure(node: Node): TempSector =
    TempSector(createNodeDoorways(node), listOf(node))

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