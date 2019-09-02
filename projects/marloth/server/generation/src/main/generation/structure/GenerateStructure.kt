package generation.structure

import generation.abstracted.*
import generation.abstracted.old.Cluster
import generation.misc.*
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.sculpting.*
import mythic.spatial.*
import simulation.misc.*

const val doorwayLength = 2.5f
const val doorLength = 1.75f
const val wallHeight = 4f
const val doorwayDepth = 0.5f

typealias Corner = Vector3

data class TempSector(val node: Node, val corners: List<Corner>) {
  init {
    assert(corners.size > 2)
  }
}

data class NodeCorner(val corner: Corner, val angle: Float) {
  val position: Vector3
    get() = corner
}

fun radialSequence(corners: List<NodeCorner>) =
    corners.asSequence().plus(NodeCorner(corners.first().corner, corners.first().angle + Pi * 2))

fun getDoorwayPoints(width: Float, node: Node, other: Node): List<Vector2> {
  val direction = (other.position - node.position).xy().normalize()
  val point = node.position.xy() + direction * node.radius
  return forkVector(point, direction, width)
}

fun getDoorwayPoints3(width: Float, node: Node, other: Node): List<Vector3> =
    getDoorwayPoints(width, node, other)
        .map { Vector3(it.x, it.y, node.position.z) }

fun getInnerDoorwayLength(doorFrameWalls: List<Id>, firstNode: Id, secondNode: Id): Float =
    if (doorFrameWalls.contains(firstNode) || doorFrameWalls.contains(secondNode))
      doorLength
    else
      doorwayLength

fun getDoorFramePoints(node: Node, other: Node): List<Vector3> =
    listOf(doorwayLength, doorLength)
        .flatMap { getDoorwayPoints3(it, node, other) }

fun getDoorwayOrDoorFramePoints(doorFrameWalls: List<Id>, node: Node, other: Node): List<Vector3> =
    if (doorFrameWalls.contains(other.id))
      getDoorFramePoints(node, other)
    else
      getDoorwayPoints3(doorwayLength, node, other)

fun createVerticesForOverlappingCircles(node: Node, other: Node, others: List<Node>): List<Corner> =
    circleIntersection(node.position.xy(), node.radius, other.position.xy(), other.radius)
        // Only needed for tri-unions
//        .filter { c -> !others.any { isInsideNodeHorizontally(c, it) } }
        .map { Vector3(it.x, it.y, node.position.z) }

fun createNodeDoorways(graph: Graph, node: Node): List<List<Vector3>> =
    connections(graph, node)
        .filter { it.type != ConnectionType.union }
        .map { getDoorwayOrDoorFramePoints(graph.doorways, node, it.other(graph, node)) }

data class CornerGap(val first: NodeCorner, val second: NodeCorner, val length: Float)

fun mapCorners(corners: List<Corner>, node: Node) =
    corners.map { NodeCorner(it, getAngle(node, it)) }

fun getGaps(corners: List<NodeCorner>, minAngle: Float) =
    toPairs(radialSequence(corners))
        .filter { it.second.angle - it.first.angle > minAngle }
        .map { CornerGap(it.first, it.second, it.second.angle - it.first.angle) }

private val minAngle = Pi * 0.3f

fun fillCornerGaps(unorderedCorners: List<Corner>, node: Node): List<Corner> {
  val corners = mapCorners(unorderedCorners, node).sortedBy { getAngle(node, it.position) }
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

fun createRoomCorners(node: Node): List<Corner> {
  val newCorners = mutableListOf<Corner>()
  val count = 6
  val entropy = node.position.x + node.position.y * 100 + node.position.z * 10000 % 1f
  val increment = Pi * 2f / count
  val start = Pi * 2f * entropy
  for (i in 1..count) {
    val angle = start + increment * i
    val position = project2D(angle, node.radius).toVector3() + node.position
    newCorners.add(position)
  }

  return newCorners
}

const val minPointDistance = 0.01f

fun withoutClosePoints(corners: List<Corner>): List<Corner> {
  val tooClose = crossMap(corners) { a: Corner, b: Corner ->
    //    println(a.distance(b))
    a.distance(b) < minPointDistance
  }
  return corners.minus(tooClose.distinct())
}

fun createSingleNodeStructure(graph: Graph, node: Node): TempSector {
  val doorways = createNodeDoorways(graph, node)
  val corners = if (doorways.any()) {
    val points = withoutClosePoints(doorways.flatten())
    points.plus(fillCornerGaps(points, node)).sortedBy { getAngle(node, it) }
  } else
    createRoomCorners(node)

  return TempSector(node, corners)
}

fun getClusterUnions(connections: InitialConnections, cluster: Cluster): List<InitialConnection> =
    cluster.flatMap { a -> connections.filter { it.contains(a) && it.type == ConnectionType.union } }.distinct()

fun fillClusterCornerGaps(unorderedCorners: List<Corner>, node: Node, otherNodes: List<Node>): List<Corner> {
  val additional = fillCornerGaps(unorderedCorners, node)
  return additional.filter { corner -> !otherNodes.any { isInsideNode(corner.xy(), it) } }
}

fun createClusterNodeStructure(node: Node, cluster: List<Node>, corners: List<Corner>,
                               doorways: List<List<Vector3>>): List<Corner> {
  val moreCorners = corners.plus(doorways.flatten())
  val allCorners = moreCorners
      .plus(fillClusterCornerGaps(moreCorners, node, cluster.filter { it !== node }))

//  return withoutClosePoints(allCorners)
  return allCorners
      .distinct()
      .sortedBy { getAngle(node, it) }
}

data class SharedCorners(val nodes: List<Node>, val corners: List<Corner>)

fun createClusterStructure(graph: Graph, cluster: Cluster): List<TempSector> {
  val allDoorways = cluster.map { createNodeDoorways(graph, it) }
  val doorwayPoints = allDoorways.flatten().flatten()
  val overlapPoints = getClusterUnions(graph.connections, cluster)
      .map { i ->
        val (first, second) = i.nodes(graph)
        SharedCorners(listOf(first, second),
            createVerticesForOverlappingCircles(first, second, cluster.filter { it.id != i.first && it.id != i.second }))
      }
      .map { points ->
        points.copy(
            corners = points.corners.map { point ->
              val nearbyDoorPoint = doorwayPoints.firstOrNull { point.distance(it) < 1f }
              if (nearbyDoorPoint != null)
                nearbyDoorPoint
              else
                point
            }
        )
      }

  return cluster.zip(allDoorways) { node, doorways ->
    val overlapping = overlapPoints.filter { a -> a.nodes.any { it === node } }.flatMap { it.corners }
    val points = createClusterNodeStructure(node, cluster, overlapping, doorways)
    TempSector(node, points)
  }
}

fun generateTunnelStructure(graph: Graph, node: Node, nodeSectors: Map<Id, TempSector>): TempSector {
  val n = neighbors(graph, node).toList()
  val sectors = n.map { nodeSectors[it.id]!! }
  assert(sectors.size == 2)
  val corners = sectors.zip(sectors.reversed()) { a, b ->
    if (graph.doorways.contains(a.node.id)) {
      val doorRoom = getOtherNode(graph, node.id, a.node.id)
      val points = getDoorFramePoints(doorRoom, a.node)
      val c = getExtrudedDoorwayPoints(points, b.node.position)
      val d = c.map { point ->
        val point2 = a.corners.sortedBy { it.xy().distance(point.xy()) }.first()
        if (point.xy().distance(point2.xy()) < 0.001f)
          point2
        else
          point
      }
      d
    } else
      getDoorwayPoints3(doorwayLength, a.node, b.node)
          .map { point -> mapPointToExisting(a.corners, point.xy()) }
//          .map { point -> a.corners.sortedBy { it.xy().distance(point.xy()) }.first() }
  }
      .flatten()
      .sortedBy { getAngle(node, it) }

  return TempSector(node, corners)
}

fun isFacingSameDirection(a: Vector3, b: Vector3): Boolean {
  val dot = a.dot(b)
  assert(dot != 0f)
  return dot > 0f
}

fun horizontalExtrusionVector(points: List<Vector3>, targetPoint: Vector3): Vector3 {
  val cross = (points[1] - points[0]).normalize().cross(Vector3(0f, 0f, 1f))
  return if (isFacingSameDirection((targetPoint - points[0]).normalize(), cross))
    cross
  else
    -cross
}

fun getExtrudedDoorwayPoints(firstCorners: List<Vector3>, otherNodePosition: Vector3): List<Vector3> {
  val extrusionVector = horizontalExtrusionVector(firstCorners, otherNodePosition)
  val offset = extrusionVector * doorwayDepth
  return firstCorners.map { it + offset }
}

fun mapPointToExisting(corners: List<Corner>, point: Vector2): Vector3 {
  val result = corners.filter { it.xy().distance(point) < 0.001f }
  assert(result.size == 1)
  return result.first()
}

fun getExistingDoorwayPoints(sector: TempSector, otherNode: Node): List<Vector3> =
    getDoorwayPoints(doorLength, sector.node, otherNode)
        .map { point -> mapPointToExisting(sector.corners, point) }

fun getDoorwayNodePoints(sector: TempSector, otherNode: Node): List<List<Vector3>> {
  val firstCorners = getExistingDoorwayPoints(sector, otherNode)
  val secondCorners = getExtrudedDoorwayPoints(firstCorners, otherNode.position)
  return listOf(firstCorners, secondCorners)
}

fun generateDoorwayStructure(graph: Graph, node: Node, nodeSectors: Map<Id, TempSector>): TempSector {
  // Room    (other)
  //  |
  // Tunnel
  //  |
  // Doorway
  //  |
  // Room    (main)

  val nodes = neighbors(graph, node).sortedBy { if (nodeSectors.containsKey(it.id)) 0 else 1 }.toList()
  val first = nodes[0]
  val mainSector = nodeSectors[first.id]!!
  val second = neighbors(graph, nodes[1]).first { it.id != first.id }
  val corners = getDoorwayNodePoints(mainSector, second)

  val finalCorners = corners.flatten()
      .sortedBy { getAngle(node, it) }

  return TempSector(node, finalCorners)
}

interface GeometryIdSources {
  val face: IdSource
  val edge: IdSource
}

//fun sinewFloorsAndCeilings(idSources: GeometryIdSources, nodeSectors: Collection<TempSector>, mesh: ImmutableMesh): List<FacePair> {
//  var currentMesh = mesh
//  return nodeSectors.flatMap { sector ->
//    val sectorCenter = getCenter(sector.corners).xy()
//    val heightOffset = Vector3(0f, 0f, wallHeight / 2f)
//    val ceilingVertices = sector.corners.map { it + heightOffset }
//    val floorVertices = sector.corners.map { it - heightOffset }
//    val result = listOf(
//        createCeiling(idSources, currentMesh, sector.node, ceilingVertices, sectorCenter),
//        createFloor(idSources, currentMesh, sector.node, floorVertices, sectorCenter)
//    )
//    currentMesh = currentMesh.copy(edges = currentMesh.edges.plus(entityMap(result.flatMap { er -> er.geometry.edges.map { it.edge } })))
//    result
//  }
//}

fun createWall(idSources: GeometryIdSources, edge: ImmutableEdge, mesh: ImmutableMesh): ImmutableFace {
  return mesh.createStitchedFace(idSources.edge, idSources.face(), listOf(
      edge.second,
      edge.first,
      edge.first + Vector3(0f, 0f, wallHeight),
      edge.second + Vector3(0f, 0f, wallHeight)
  ))
}

//fun toEdgeTable(faces: Collection<ImmutableFace>) =
//    entityMap(faces.flatMap { er -> er.edges.map { it.edge } }.distinct())

data class Disjoint(
    val sector: Id,
    val point: Vector3
)

fun getDisjoints(graph: Graph, sectors: Map<Id, TempSector>): List<Pair<Disjoint, Disjoint>> =
    graph.connections.map { connection ->
      val firstSector = sectors[connection.first]!!
      val secondSector = sectors[connection.second]!!
      firstSector.corners.map { a ->
        secondSector.corners.filter { b -> a != b && a.distance(b) < 0.01f }
            .map {
              val sorted = listOf(Disjoint(connection.first, a), Disjoint(connection.second, it))
                  .sortedBy { it.hashCode() }
              Pair(sorted[0], sorted[1])
            }
      }
          .flatten()
    }
        .flatten()

fun fixDisjoints(sectors: Map<Id, TempSector>, disjoints: List<Pair<Disjoint, Disjoint>>) =
    sectors.mapValues { (id, sector) ->
      val corrections = disjoints.filter { it.first.sector == id }
      if (corrections.none())
        sector
      else
        sector.copy(
            corners = sector.corners.map { corner ->
              val correction = corrections.firstOrNull { it.second.point == corner }
              if (correction != null)
                correction.first.point
              else
                corner
            }
        )
    }
