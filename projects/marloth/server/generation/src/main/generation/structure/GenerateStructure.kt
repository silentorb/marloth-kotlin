package generation.structure

import generation.*
import generation.abstract.*
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import randomly.Dice
import simulation.FaceType

const val doorwayLength = 2.5f
const val wallHeight = 4f

typealias Corner = Vector3m

data class TempSector(val node: Node, val corners: List<Corner>)
data class NodeCorner(val corner: Corner, val angle: Float) {
  val position: Vector3m
    get() = corner
}

fun radialSequence(corners: List<NodeCorner>) =
    corners.asSequence().plus(NodeCorner(corners.first().corner, corners.first().angle + Pi * 2))

fun getDoorwayPoints(node: Node, other: Node): List<Vector2> {
  val direction = (other.position - node.position).xy().normalize()
  val point = node.position.xy() + direction * node.radius
  return forkVector(point, direction, doorwayLength)
}

fun createDoorway(node: Node, other: Node) =
    getDoorwayPoints(node, other)
        .map { it.toVector3() }

fun createVerticesForOverlappingCircles(node: Node, other: Node, others: List<Node>): List<Corner> =
    circleIntersection(node.position.xy(), node.radius, other.position.xy(), other.radius)
        // Only needed for tri-unions
//        .filter { c -> !others.any { isInsideNodeHorizontally(c, it) } }
        .map { it.toVector3() }

fun createNodeDoorways(graph: Graph, node: Node) =
    node.connections(graph)
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

const val minPointDistance = 1f

fun withoutClosePoints(corners: List<Corner>): List<Corner> {
  val tooClose = crossMap(corners.asSequence()) { a: Corner, b: Corner ->
    //    println(a.distance(b))
    a.distance(b) < minPointDistance
  }
  return corners.minus(tooClose.distinct())
}

fun createSingleNodeStructure(graph: Graph, node: Node): TempSector {
  val doorways = createNodeDoorways(graph, node)
  val points = withoutClosePoints(doorways.flatten())
  return TempSector(node, points.plus(fillCornerGaps(points, node)).sortedBy { getAngle(node, it) })
}

fun getClusterUnions(graph: Graph, cluster: Cluster): List<Connection> =
    cluster.flatMap { a -> a.connections(graph).filter { it.type == ConnectionType.union } }.distinct()

fun fillClusterCornerGaps(unorderedCorners: List<Corner>, node: Node, otherNodes: List<Node>): List<Corner> {
  val additional = fillCornerGaps(unorderedCorners, node)
  return additional.filter { corner -> !otherNodes.any { isInsideNode(corner.xy(), it) } }
}

fun createClusterNodeStructure(graph: Graph, node: Node, cluster: List<Node>, corners: List<Corner>): List<Corner> {
  val doorways = createNodeDoorways(graph, node)
  val moreCorners = corners.plus(doorways.flatten())
  val allCorners = moreCorners
      .plus(fillClusterCornerGaps(moreCorners, node, cluster.filter { it !== node }))

//  return withoutClosePoints(allCorners)
  return allCorners
      .sortedBy { getAngle(node, it) }
}

data class SharedCorners(val nodes: List<Node>, val corners: List<Corner>)

fun createClusterStructure(graph: Graph, cluster: Cluster): List<TempSector> {
  val overlapPoints = getClusterUnions(graph, cluster)
      .map { i ->
        SharedCorners(listOf(i.first, i.second),
            createVerticesForOverlappingCircles(i.first, i.second, cluster.filter { it !== i.first && it !== i.second }))
      }

  return cluster.map { node ->
    val overlapping = overlapPoints.filter { a -> a.nodes.any { it === node } }.flatMap { it.corners }
    TempSector(node, createClusterNodeStructure(graph, node, cluster, overlapping))
  }
}

fun generateTunnelStructure(graph: Graph, node: Node, nodeSectors: List<TempSector>): TempSector {
  val (first, second) = node.neighbors(graph).toList()
  val sectors = nodeSectors.filter { it.node === first || it.node == second }
  val corners = sectors.flatMap { sector ->
    val other = if (sector.node === first) second else first
    val points = getDoorwayPoints(sector.node, other)
    points.map { point -> sector.corners.sortedBy { it.xy().distance(point) }.first() }
  }
      .sortedBy { getAngle(node, it) }

  return TempSector(node, corners)
}

fun sinewFloorsAndCeilings(nodeSectors: List<TempSector>, mesh: FlexibleMesh) {
  return nodeSectors.forEach { sector ->
    val sectorCenter = getCenter(sector.corners).xy()
    createFloor(mesh, sector.node, sector.corners, sectorCenter)
    createCeiling(mesh, sector.node, sector.corners, sectorCenter)
  }
}

fun createWall(edge: FlexibleEdge, mesh: FlexibleMesh): FlexibleFace {
  return mesh.createStitchedFace(listOf(
      edge.second,
      edge.first,
      edge.first + Vector3m(0f, 0f, wallHeight),
      edge.second + Vector3m(0f, 0f, wallHeight)
  ))
}

fun getOrCreateWall(edge: FlexibleEdge, otherEdges: List<EdgeReference>, mesh: FlexibleMesh): FlexibleFace {
  return if (otherEdges.size > 1) {
    val face = edge.faces.firstOrNull { it.data != null && getFaceInfo(it).type == FaceType.space }
    if (face != null)
      face
    else
      createWall(edge, mesh)
  } else
    createWall(edge, mesh)
}

fun createRooms(realm: Realm, dice: Dice, tunnels: List<Node>) {
  val mesh = realm.mesh

  val roomNodes = realm.nodes.minus(tunnels)
  val graph = realm.graph

  val singleNodes = roomNodes.filter { !isInCluster(graph, it) }
  val clusters = gatherClusters(graph, roomNodes)
  val nodeSectors = singleNodes.map { createSingleNodeStructure(graph, it) }
      .plus(clusters.flatMap { createClusterStructure(graph, it) })

  val tunnelSectors = tunnels
      .map { generateTunnelStructure(graph, it, nodeSectors) }

  val allSectors = nodeSectors.plus(tunnelSectors)
  sinewFloorsAndCeilings(allSectors, mesh)
  allSectors.forEach { sector ->
    val wallBases = sector.node.floors.first().edges
    wallBases.forEach { edgeReference ->
      val otherEdges = edgeReference.otherEdgeReferences
      val wall = getOrCreateWall(edgeReference.edge, otherEdges, mesh)

      if (otherEdges.any())
        initializeFaceInfo(FaceType.space, sector.node, wall)
      else
        initializeFaceInfo(FaceType.wall, sector.node, wall)

      sector.node.walls.add(wall)
    }
  }
}

fun generateStructure(realm: Realm, dice: Dice, tunnels: List<Node>) {
  createRooms(realm, dice, tunnels)
  calculateNormals(realm.mesh)
  initializeFaceInfo(realm)
  val roomNodes = realm.nodes.toList()
  defineNegativeSpace(realm, dice)
  fillBoundary(realm, dice)
  expandVertically(realm, roomNodes, dice)
}