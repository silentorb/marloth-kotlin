package generation.structure

import generation.*
import generation.abstract.*
import generation.abstract.Realm
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import physics.voidNodeId
import randomly.Dice
import simulation.*

const val doorwayLength = 2.5f
const val wallHeight = 4f

typealias Corner = Vector3

data class TempSector(val node: Node, val corners: List<Corner>)
data class NodeCorner(val corner: Corner, val angle: Float) {
  val position: Vector3
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
        .map { Vector3(it.x, it.y, 0f) }

fun createVerticesForOverlappingCircles(node: Node, other: Node, others: List<Node>): List<Corner> =
    circleIntersection(node.position.xy(), node.radius, other.position.xy(), other.radius)
        // Only needed for tri-unions
//        .filter { c -> !others.any { isInsideNodeHorizontally(c, it) } }
        .map { Vector3(it.x, it.y, 0f) }

fun createNodeDoorways(graph: Graph, node: Node) =
    node.connections(graph)
        .filter { it.type != ConnectionType.union }
        .map { createDoorway(node, it.getOther(graph, node)) }

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
      newCorners.add(position)
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

fun getClusterUnions(connections: Connections, cluster: Cluster): List<Connection> =
    cluster.flatMap { a -> connections.filter { it.contains(a) && it.type == ConnectionType.union } }.distinct()

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
  val overlapPoints = getClusterUnions(graph.connections, cluster)
      .map { i ->
        val (first, second) = i.nodes(graph)
        SharedCorners(listOf(first, second),
            createVerticesForOverlappingCircles(first, second, cluster.filter { it.id != i.first && it.id != i.second }))
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

fun sinewFloorsAndCeilings(nextId: IdSource, nodeSectors: List<TempSector>, mesh: ImmutableMesh): List<FacePair> {
  return nodeSectors.flatMap { sector ->
    val sectorCenter = getCenter(sector.corners).xy()
    val vertices = sector.corners.map { it }
    listOf(createFloor(mesh, nextId, sector.node, vertices, sectorCenter),
        createCeiling(mesh, nextId, sector.node, vertices, sectorCenter)
    )
  }
}

fun createWall(nextId: IdSource, edge: ImmutableEdge, mesh: ImmutableMesh): ImmutableFace {
  return mesh.createStitchedFace(nextId(), listOf(
      edge.second,
      edge.first,
      edge.first + Vector3(0f, 0f, wallHeight),
      edge.second + Vector3(0f, 0f, wallHeight)
  ))
}

fun getOrCreateWall(faces: FaceTable, nextId: IdSource, edge: ImmutableEdge, otherEdges: List<ImmutableEdgeReference>, mesh: ImmutableMesh): ImmutableFace {
  return if (otherEdges.size > 1) {
    val face = edge.faces.firstOrNull {
      val info = faces[it.id]
      info != null && info.faceType == FaceType.space
    }
    if (face != null)
      face
    else
      createWall(nextId, edge, mesh)
  } else
    createWall(nextId, edge, mesh)
}

fun createRooms(realm: Realm, nextFaceId: IdSource, dice: Dice, tunnels: List<Node>): List<NodeFace> {
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
  val floorsAndCeilings = sinewFloorsAndCeilings(nextFaceId, allSectors, mesh)
  val faces: FaceTable = mapOf()

  val pairs = allSectors.flatMap { sector ->
    val wallBases = sector.node.floors.first().edges
    wallBases.map { immutableEdgeReference ->
      Pair(immutableEdgeReference, sector.node.id)
    }
  }

  val nodeTable = entityMap(realm.nodes)
  val groups = pairs.groupBy { it.first.edge.hashCode() }
  val (singles, shared) = groups.entries.partition { it.value.size == 1 }
  val updatedWalls = singles.map { it.value.first() }.map {
    val face = createWall(nextFaceId, it.first.edge, mesh)
    nodeTable[it.second]!!.walls.add(face)
    NodeFace(face.id, FaceType.wall, it.second, voidNodeId, null)
  }
      .plus(
          shared.map { it.value }.map {
            val face = createWall(nextFaceId, it.first().first.edge, mesh)
            nodeTable[it[0].second]!!.walls.add(face)
            nodeTable[it[1].second]!!.walls.add(face)
            NodeFace(face.id, FaceType.space, it[0].second, it[1].second, null)
          }
      )
//  val updatedWalls = allSectors.flatMap { sector ->
//    val wallBases = sector.node.floors.first().edges
//    wallBases.map { ImmutableEdgeReference ->
//      val otherEdges = ImmutableEdgeReference.otherImmutableEdgeReferences
//      val wall = getOrCreateWall(faces, nextFaceId, ImmutableEdgeReference.edge, otherEdges, mesh)
//
//      val info = if (otherEdges.any())
//        initializeFaceInfo(faces, FaceType.space, sector.node, wall)
//      else
//        initializeFaceInfo(faces, FaceType.wall, sector.node, wall)
//
//      sector.node.walls.add(wall)
//      info
//    }
//  }
  return floorsAndCeilings.map { it.info }.plus(updatedWalls)
}

fun generateStructure(realm: Realm, nextFaceId: IdSource, dice: Dice, tunnels: List<Node>): Pair<Graph, RealmMesh> {
  val roomFaces = entityMap(createRooms(realm, nextFaceId, dice, tunnels))
  calculateNormals(realm.mesh)
//  initializeFaceInfo(realm)
  val roomNodes = realm.nodes.toList()
  val roomsMesh = RealmMesh(roomFaces)
  val (negativeGraph, negativeMesh) = defineNegativeSpace(realm, roomsMesh, nextFaceId, dice)
  val (boundaryGraph, updatedFaces) = fillBoundary(realm.copy(graph = negativeGraph), negativeMesh, nextFaceId, dice)
  val mesh = RealmMesh(
      faces = updatedFaces
  )
  return expandVertically(realm.copy(graph = boundaryGraph), mesh, realm.nodes.associate { Pair(it.id, it) }, nextFaceId, roomNodes, dice)
}