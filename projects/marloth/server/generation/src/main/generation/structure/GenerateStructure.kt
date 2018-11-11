package generation.structure

import generation.*
import generation.abstracted.*
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.entityMap
import mythic.ent.pipeline
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import physics.voidNodeId
import randomly.Dice
import simulation.*

const val doorwayLength = 2.5f
const val doorLength = 1.75f
const val wallHeight = 4f

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
        .map { Vector3(it.x, it.y, 0f) }

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
        .map { Vector3(it.x, it.y, 0f) }

fun createNodeDoorways(graph: Graph, node: Node) =
    connections(graph, node)
        .filter { it.type != ConnectionType.union }
        .map { getDoorwayOrDoorFramePoints(graph.doorways, node, it.getOther(graph, node)) }

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

fun getClusterUnions(connections: InitialConnections, cluster: Cluster): List<InitialConnection> =
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

fun generateTunnelStructure(graph: Graph, node: Node, nodeSectors: Map<Id, TempSector>): TempSector {
  val n = neighbors(graph, node).toList()
  val sectors = n.map { nodeSectors[it.id]!! }
  assert(sectors.size == 2)
  val corners = sectors.zip(sectors.reversed()) { a, b ->
    if (graph.doorways.contains(a.node.id)) {
//      getDoorwayNodePoints(a, b.node)[0]
      val doorRoom = getOtherNode(graph, node.id, a.node.id)
      val points = getDoorFramePoints(doorRoom, a.node)
      getExtrudedDoorwayPoints(points, b.node.position)
    } else
      getDoorwayPoints3(doorwayLength, a.node, b.node)
          .map { point -> a.corners.sortedBy { it.xy().distance(point.xy()) }.first() }
  }
      .flatten()
      .sortedBy { getAngle(node, it) }

  return TempSector(node, corners)
}

fun isFacingSameGeneralDirection(a: Vector3, b: Vector3): Boolean {
  val dot = a.dot(b)
  assert(dot != 0f)
  return dot > 0f
}

fun horizontalExtrusionVector(points: List<Vector3>, targetPoint: Vector3): Vector3 {
  val cross = (points[1] - points[0]).normalize().cross(Vector3(0f, 0f, 1f))
  return if (isFacingSameGeneralDirection((targetPoint - points[0]).normalize(), cross))
    cross
  else
    -cross
}

fun getExtrudedDoorwayPoints(firstCorners: List<Vector3>, otherNodePosition: Vector3): List<Vector3> {
  val extrusionVector = horizontalExtrusionVector(firstCorners, otherNodePosition)
  val doorwayDepth = 0.5f
  val offset = extrusionVector * doorwayDepth
  return firstCorners.map { it + offset }
}

fun getExistingDoorwayPoints(sector: TempSector, otherNode: Node): List<Vector3> =
    getDoorwayPoints(doorLength, sector.node, otherNode)
        .map { point -> sector.corners.sortedBy { it.xy().distance(point) }.first() }

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

fun sinewFloorsAndCeilings(idSources: GeometryIdSources, nodeSectors: Collection<TempSector>, mesh: ImmutableMesh): List<FacePair> {
  var currentMesh = mesh
  return nodeSectors.flatMap { sector ->
    val sectorCenter = getCenter(sector.corners).xy()
    val vertices = sector.corners.map { it }
    val result = listOf(createFloor(idSources, currentMesh, sector.node, vertices, sectorCenter),
        createCeiling(idSources, currentMesh, sector.node, vertices, sectorCenter)
    )
    currentMesh = currentMesh.copy(edges = currentMesh.edges.plus(entityMap(result.flatMap { er -> er.geometry.edges.map { it.edge } })))
    result
  }
}

fun createWall(idSources: GeometryIdSources, edge: ImmutableEdge, mesh: ImmutableMesh): ImmutableFace {
  return mesh.createStitchedFace(idSources.edge, idSources.face(), listOf(
      edge.second,
      edge.first,
      edge.first + Vector3(0f, 0f, wallHeight),
      edge.second + Vector3(0f, 0f, wallHeight)
  ))
}

fun toEdgeTable(faces: Collection<ImmutableFace>) =
    entityMap(faces.flatMap { er -> er.edges.map { it.edge } }.distinct())


fun createRooms(graph: Graph, idSources: GeometryIdSources, dice: Dice): StructureRealm {
  val roomNodes = graph.nodes.minus(graph.tunnels).minus(graph.doorways)

  val singleNodes = roomNodes.values.filter { !isInCluster(graph, it) }
  val clusters = gatherClusters(graph, roomNodes.values)
  val roomSectors = singleNodes.map { createSingleNodeStructure(graph, it) }
      .plus(clusters.flatMap { createClusterStructure(graph, it) })
      .associate { Pair(it.node.id, it) }

  val doorwaySectors = graph.doorways
      .map { generateDoorwayStructure(graph, graph.nodes[it]!!, roomSectors) }

  val nodeSectors = roomSectors.plus(doorwaySectors.associate { Pair(it.node.id, it) })

  val tunnelSectors = graph.tunnels
      .map { generateTunnelStructure(graph, graph.nodes[it]!!, nodeSectors) }
      .associate { Pair(it.node.id, it) }

  val allSectors = nodeSectors
      .plus(tunnelSectors)

  val floorsAndCeilings = splitFacePairTables(sinewFloorsAndCeilings(idSources, allSectors.values, ImmutableMesh()))
  var mesh = ImmutableMesh(
      faces = floorsAndCeilings.second,
      edges = toEdgeTable(floorsAndCeilings.second.values)
  )

  val pairs = allSectors.values.flatMap { sector ->
    val wallBases = sector.node.floors.first().edges
    wallBases.map { immutableEdgeReference ->
      Pair(immutableEdgeReference, sector.node.id)
    }
  }

  val nodeTable = graph.nodes
  val groups = pairs.groupBy { it.first.edge.id }
  val (singles, shared) = groups.entries.partition { it.value.size == 1 }
  val updatedWalls = singles.map { it.value.first() }.map {
    val face = createWall(idSources, it.first.edge, mesh)
    mesh = mesh.copy(
        faces = mesh.faces.plus(Pair(face.id, face)),
        edges = mesh.edges.plus(entityMap(face.edges.map { it.edge }))
    )
    nodeTable[it.second]!!.walls.add(face)
    FacePair(ConnectionFace(face.id, FaceType.wall, it.second, voidNodeId, null), face)
  }
      .plus(
          shared.map { it.value }.map {
            val face = createWall(idSources, it.first().first.edge, mesh)
            mesh = mesh.copy(
                faces = mesh.faces.plus(Pair(face.id, face)),
                edges = mesh.edges.plus(entityMap(face.edges.map { it.edge }))
            )
            nodeTable[it[0].second]!!.walls.add(face)
            nodeTable[it[1].second]!!.walls.add(face)
            FacePair(ConnectionFace(face.id, FaceType.space, it[0].second, it[1].second, null), face)
          }
      )
  return StructureRealm(
      nodes = nodeTable,
      connections = floorsAndCeilings.first.plus(entityMap(updatedWalls.map { it.info })),
      mesh = mesh
  )
}

data class StructureRealm(
    val nodes: NodeTable,
    val connections: ConnectionTable,
    val mesh: ImmutableMesh
)

fun requiresSolidNeighbors(realm: StructureRealm, nodeId: Id): Boolean =
    if (nodeId == voidNodeId)
      false
    else {
      val neighbor = realm.nodes[nodeId]!!
      val biomeInfo = biomeInfoMap[neighbor.biome]!!
      biomeInfo.enclosureRate == 1f
    }

fun isNodeSolid(dice: Dice, realm: StructureRealm, node: Node): Boolean {
  val enclosureRate = biomeInfoMap[node.biome]!!.enclosureRate
  if (horizontalNeighbors(realm.connections, node).any { requiresSolidNeighbors(realm, it) })
    return true

  return if (enclosureRate == 1f)
    true
  else if (enclosureRate == 0f)
    false
  else
    dice.getFloat(1f) < enclosureRate
}

fun fillNodeBiomesAndSolid(dice: Dice, realm: StructureRealm, biomeGrid: BiomeGrid): NodeTable =
    realm.nodes.mapValues { (_, node) ->
      if (node.biome == Biome.void) {
        val biome = biomeGrid(node.position.x, node.position.y)
        node.copy(
            biome = biome,
            isSolid = isNodeSolid(dice, realm, node)
        )
      } else
        node
    }

fun generateStructure(biomeGrid: BiomeGrid, idSources: StructureIdSources, graph: Graph, dice: Dice): StructureRealm {
  val initialRealm = createRooms(graph, idSources, dice)
  val roomNodes = graph.nodes
  return pipeline(initialRealm, listOf(
      { realm -> defineNegativeSpace(idSources, realm, dice) }
//      { realm -> realm.copy(nodes = fillNodeBiomesAndSolid(dice, realm, biomeGrid)) },
//      { realm -> fillBoundary(idSources, realm, dice) },
//      { realm -> expandVertically(idSources, realm, roomNodes.values, dice) },
//      { realm ->
//        realm.copy(
//            mesh = realm.mesh.copy(
//                edges = entityMap(realm.mesh.faces.flatMap { er -> er.value.edges.map { it.edge } }.distinct())
//            )
//        )
//      }
  ))
}