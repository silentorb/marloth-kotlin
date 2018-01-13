package generation

import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.times
import mythic.spatial.toVector3
import org.joml.minus
import org.joml.plus
import org.joml.xy

fun createDoorway(node: Node, other: Node, mesh: HalfEdgeMesh) {
  val direction = (other.position - node.position).xy.normalize()
  val point = node.position.xy + direction * node.radius
  val points = forkVector(point, direction, 1.5f)
  mesh.addVertex(points.first.toVector3())
  mesh.addVertex(points.second.toVector3())
}

fun createVerticesForOverlappingCircles(node: Node, other: Node, mesh: HalfEdgeMesh) {
  val points = circleIntersection(node.position.xy, node.radius, other.position.xy, other.radius)
  mesh.addVertex(points.first.toVector3())
  mesh.addVertex(points.second.toVector3())
}

fun createNodeDoorways(node: Node, mesh:HalfEdgeMesh) {
  for (connection in node.connections.filter { it.type != ConnectionType.union }) {
    val other = connection.getOther(node)
    createDoorway(node, other, mesh)
  }
}

fun createSingleNodeStructure(node: Node, mesh: HalfEdgeMesh) {
  createNodeDoorways(node, mesh)
}

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


fun createClusterStructure(cluster: Cluster, mesh: HalfEdgeMesh) {
  val unions = cluster.map { it.connections.filter { it.type == ConnectionType.union } }.flatten().distinct()

  for (connection in unions) {
    createVerticesForOverlappingCircles(connection.first, connection.second, mesh)
  }

  for (node in cluster) {
    createNodeDoorways(node, mesh)
  }
}

fun generateStructure(abstractWorld: AbstractWorld, structureWorld: StructureWorld) {
  val mesh = structureWorld.mesh

  val singleNodes = abstractWorld.nodes.filter { !isInCluster(it) }
  singleNodes.forEach { createSingleNodeStructure(it, mesh) }

  val clusters = gatherClusters(abstractWorld.nodes)
  clusters.forEach { createClusterStructure(it, mesh) }
}