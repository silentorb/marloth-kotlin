package generation.structure

import generation.*
import generation.abstract.Cluster
import generation.abstract.gatherClusters
import generation.abstract.isInCluster
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.xy
import randomly.Dice
import scenery.Textures
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
  val direction = (other.position - node.position).xy.normalize()
  val point = node.position.xy + direction * node.radius
  return forkVector(point, direction, doorwayLength)
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
    for (i in 1..count) {
      val angle = gap.first.angle + increment * i
      val position = project2D(angle, node.radius).toVector3() + node.position
      newCorners.add(Corner(position))
    }
  }

  return newCorners
}

fun createSingleNodeStructure(node: Node): TempSector {
  val doorways = createNodeDoorways(node)
  val points = doorways.flatten()
  return TempSector(node, points.plus(fillCornerGaps(points, node)).sortedBy { getAngle(node, it) })
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

fun createClusterStructure(cluster: Cluster): List<TempSector> {
  val overlapPoints = getClusterUnions(cluster)
      .map { i ->
        SharedCorners(listOf(i.first, i.second),
            createVerticesForOverlappingCircles(i.first, i.second, cluster.filter { it !== i.first && it !== i.second }))
      }

  return cluster.map { node ->
    val overlapping = overlapPoints.filter { it.nodes.any { it === node } }.flatMap { it.corners }
    TempSector(node, createClusterNodeStructure(node, cluster, overlapping))
  }
}

fun generateTunnelStructure(node: Node, nodeSectors: List<TempSector>): TempSector {
  val (first, second) = node.neighbors.toList()
  val sectors = nodeSectors.filter { it.node === first || it.node == second }
  val corners = sectors.flatMap { sector ->
    val points = getDoorwayPoints(sector.node, if (sector.node === first) second else first)
    sector.corners.filter { p -> points.any { roughlyEquals(it, p.xy, 0.1f) } }
  }
      .sortedBy { getAngle(node, it) }

  return TempSector(node, corners)
}

fun createFloor(mesh: FlexibleMesh, node: Node, vertices: Vertices, center: Vector2): FlexibleFace {
  val sortedFloorVertices = vertices
      .sortedBy { atan(it.xy - center) }
  val floor = mesh.createStitchedFace(sortedFloorVertices)
  floor.data = FaceInfo(FaceType.floor, node, null)
  node.floors.add(floor)
  return floor
}

fun createCeiling(mesh: FlexibleMesh, node: Node, vertices: Vertices, center: Vector2): FlexibleFace {
  val sortedFloorVertices = vertices
      .sortedByDescending { atan(it.xy - center) }
      .map { it + Vector3(0f, 0f, wallHeight) }

  val surface = mesh.createStitchedFace(sortedFloorVertices)
  node.ceilings.add(surface)
  surface.data = FaceInfo(FaceType.ceiling, node, null)
  return surface
}

fun createWall(abstractWorld: AbstractWorld, node: Node, vertices: Vertices): FlexibleFace {
  val wall = abstractWorld.mesh.createStitchedFace(vertices)
  wall.data = FaceInfo(FaceType.wall, node, null)
  node.walls.add(wall)
  return wall
}

fun sinewFloorsAndCeilings(nodeSectors: List<TempSector>, mesh: FlexibleMesh) {
  return nodeSectors.forEach { sector ->
    val sectorCenter = getCenter(sector.corners).xy
    createFloor(mesh, sector.node, sector.corners, sectorCenter)
    createCeiling(mesh, sector.node, sector.corners, sectorCenter)
  }
}

fun createWall(edge: FlexibleEdge, mesh: FlexibleMesh): FlexibleFace {
  return mesh.createStitchedFace(listOf(
      edge.second,
      edge.first,
      edge.first + Vector3(0f, 0f, wallHeight),
      edge.second + Vector3(0f, 0f, wallHeight)
  ))
}

fun createRooms(abstractWorld: AbstractWorld, dice: Dice, tunnels: List<Node>) {
  val mesh = abstractWorld.mesh

  val roomNodes = abstractWorld.nodes.minus(tunnels)

  val singleNodes = roomNodes.filter { !isInCluster(it) }
  val clusters = gatherClusters(roomNodes)
  val nodeSectors = singleNodes.map { createSingleNodeStructure(it) }
      .plus(clusters.flatMap { createClusterStructure(it) })

  val tunnelSectors = tunnels
      .map { generateTunnelStructure(it, nodeSectors) }

  val allSectors = nodeSectors.plus(tunnelSectors)
  sinewFloorsAndCeilings(allSectors, mesh)
  allSectors.forEach { sector ->
    val wallBases = sector.node.floors.first().edges
    wallBases.forEach {
      val otherEdges = it.otherEdgeReferences
      val wall = if (otherEdges.size > 1) {
        val face = it.faces.firstOrNull() { it.data != null && getFaceInfo(it).type == FaceType.space }
        if (face != null)
          face
        else
          createWall(it.edge, mesh)
      } else
        createWall(it.edge, mesh)

      if (otherEdges.size > 0)
        initializeFaceInfo(FaceType.space, sector.node, wall, null)
      else
        initializeFaceInfo(FaceType.wall, sector.node, wall, Textures.darkCheckers)

      sector.node.walls.add(wall)
    }
  }
}

fun determineFloorTexture(info: FaceInfo): Textures? {
  val first = info.firstNode!!
  return if (first.isWalkable)
    Textures.checkers
  else
    null
}

fun determineWallTexture(info: FaceInfo): Textures? {
  val nodes = faceNodes(info)
      .filterNotNull()

  assert(nodes.any())
  return if (nodes.size == 1) {
//    if (nodes.first().isSolid)
//      Textures.checkers
//    else
      null//Textures.debugCyan
  } else {
    val wallCount = nodes.count { it.isSolid }
    val walkableCount = nodes.count { it.isWalkable }
    if (wallCount > 0 && walkableCount != 2)
      Textures.darkCheckers
    else
      null
  }
//  val rooms = nodes
//      .filter { it.type.isWalkable }
//  return if (rooms.size == 1) {
//    val other = getOtherNode(info, rooms.first())
//    if (other == null)
//      Textures.checkers
//    else if (other.type == NodeType.solid)
//      Textures.darkCheckers
//    else
//      null
//  } else if (rooms.size == 2) {
//    null
//  } else {
//    if (nodes.count { it.type == NodeType.solid } == 1)
//      Textures.darkCheckers
//    else
//      null
//  }
}

fun determineCeilingTexture(info: FaceInfo): Textures? {
  val first = info.firstNode!!
  val second = info.secondNode
  return if (second != null && second.isSolid)
    Textures.checkers
  else
    null
}

fun determineFaceTexture(info: FaceInfo): Textures? {
  return when (info.type) {
    FaceType.wall -> determineWallTexture(info)
    FaceType.floor -> determineFloorTexture(info)
    FaceType.space -> null
    FaceType.ceiling -> determineCeilingTexture(info)
  }
}

fun assignTextures(abstractWorld: AbstractWorld) {
  abstractWorld.mesh.faces.forEach { face ->
    val info = getFaceInfo(face)
    info.texture = determineFaceTexture(info)
  }
}

interface VerticalFacing {
  val dirMod: Float
  fun ceilings(node: Node): MutableList<FlexibleFace>
  fun floors(node: Node): MutableList<FlexibleFace>
  fun upperNode(node: Node): Node
  fun wallVertices(face: FlexibleFace): WallVertices
}

class VerticalFacingUp : VerticalFacing {
  override val dirMod: Float get() = 1f
  override fun ceilings(node: Node): MutableList<FlexibleFace> = node.ceilings
  override fun floors(node: Node): MutableList<FlexibleFace> = node.floors
  override fun upperNode(node: Node): Node = getUpperNode(node)
  override fun wallVertices(face: FlexibleFace): WallVertices = getWallVertices(face)
}

class VerticalFacingDown : VerticalFacing {
  override val dirMod: Float get() = -1f
  override fun ceilings(node: Node): MutableList<FlexibleFace> = node.floors
  override fun floors(node: Node): MutableList<FlexibleFace> = node.ceilings
  override fun upperNode(node: Node): Node = getLowerNode(node)
  override fun wallVertices(face: FlexibleFace): WallVertices {
    val result = getWallVertices(face)
    return WallVertices(upper = result.lower, lower = result.upper)
  }
}

fun createVerticalNodes(abstractWorld: AbstractWorld, middleNodes: List<Node>, roomNodes: List<Node>, dice: Dice, facing: VerticalFacing) {
  val newNodes = middleNodes.map { node ->
    val depth = 2f
    val offset = Vector3(0f, 0f, depth * facing.dirMod)
    val isSolid = if (!roomNodes.contains(node) && !node.isSolid)
      false
    else if (dice.getInt(0, 3) != 0)
      true
    else
      false

    val newNode = createSecondaryNode(node.position + offset, abstractWorld, isSolid = isSolid)
    assert(facing.ceilings(node).any())
    for (ceiling in facing.ceilings(node)) {
      facing.floors(newNode).add(ceiling)
      val info = getFaceInfo(ceiling)
      info.secondNode = newNode
    }
    newNode
  }

  newNodes.forEach { lowerNode ->
    addSpaceNode(abstractWorld, lowerNode)
  }
}

fun getLowerNode(node: Node) =
    getOtherNode(node, node.floors.first())!!

fun getUpperNode(node: Node) =
    getOtherNode(node, node.ceilings.first())!!

fun createDescendingSpaceWalls(abstractWorld: AbstractWorld, nodes: List<Node>, facing: VerticalFacing) {
  val walls = nodes.flatMap { it.walls }
//  nodes.forEach { node ->
  //    val walls = node.walls.filter {
//      val otherNode = getOtherNode(node, it)
//      otherNode != null && !otherNode.isSolid
//    }
//    if (walls.any()) {
  val depth = 6f
  val offset = Vector3(0f, 0f, depth * facing.dirMod)
//  val lowerNode = facing.upperNode(node)
  walls.forEach { upperWall ->
    val info = getFaceInfo(upperWall)
    if (info.secondNode != null) {
      val node = if (info.firstNode!!.isWalkable)
        info.firstNode!!
      else
        info.secondNode!!

      val upperNode = facing.upperNode(node)
      val otherUpperNode = getOtherNode(node, upperWall)!!
      val otherUpNode = facing.upperNode(otherUpperNode)
      val firstEdge = facing.wallVertices(upperWall).upper
      val unorderedVertices = firstEdge.plus(firstEdge.map { it + offset })
      val emptyNode = if (!upperNode.isSolid)
        upperNode
      else
        otherUpNode

      val orderedVertices = sortWallVertices(emptyNode.position, unorderedVertices)
      val newWall = abstractWorld.mesh.createStitchedFace(orderedVertices)
      newWall.data = FaceInfo(FaceType.wall, upperNode, otherUpNode, null, "lower")
      upperNode.walls.add(newWall)
      otherUpNode.walls.add(newWall)
    }
  }
//  }
//  }
}

fun expandVertically(abstractWorld: AbstractWorld, roomNodes: List<Node>, dice: Dice) {
  val middleNodes = abstractWorld.nodes.toList()
  listOf(VerticalFacingDown(), VerticalFacingUp())
      .forEach { facing ->
        createVerticalNodes(abstractWorld, middleNodes, roomNodes, dice, facing)
        createDescendingSpaceWalls(abstractWorld, middleNodes, facing)
      }
}

fun generateStructure(abstractWorld: AbstractWorld, dice: Dice, tunnels: List<Node>) {
  createRooms(abstractWorld, dice, tunnels)
  calculateNormals(abstractWorld.mesh)
  initializeFaceInfo(abstractWorld)
  val roomNodes = abstractWorld.nodes.toList()
  defineNegativeSpace(abstractWorld, dice)
  fillBoundary(abstractWorld, dice)
  expandVertically(abstractWorld, roomNodes, dice)
  assignTextures(abstractWorld)
}