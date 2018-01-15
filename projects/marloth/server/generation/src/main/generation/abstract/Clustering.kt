package generation.abstract

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
