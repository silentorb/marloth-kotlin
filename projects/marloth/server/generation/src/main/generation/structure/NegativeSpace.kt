package generation.structure

import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import randomly.Dice
import simulation.*

fun <T> zeroIfNull(value: T?) =
    if (value == null)
      0
    else
      1

fun faceNodeCount(faceInfo: FaceInfo) =
    zeroIfNull(faceInfo.firstNode) + zeroIfNull(faceInfo.secondNode)

fun faceNodeCount(face: FlexibleFace) =
    faceNodeCount(getFaceInfo(face))

//fun getNextFace(face: FlexibleFace): FlexibleFace? {
//  return face.edges.filter()
//  val info = getFaceInfo(face)
//  val node = info.firstNode!!
//  return node.walls.filter { it != face && it.vertices.union(face.vertices).size >= 2 }.firstOrNull()
//}

fun getSharedEdge(first: FlexibleFace, second: FlexibleFace): FlexibleEdge =
    first.edges.first { edge -> second.edges.map { it.edge }.contains(edge.edge) }.edge

// This algorithm only works on quads
fun getOppositeQuadEdge(first: FlexibleFace, edge: FlexibleEdge) =
    first.edges.first { it.vertices.none { edge.vertices.contains(it) } }

fun isConcaveCorner(first: FlexibleFace, second: FlexibleFace): Boolean {
  val sharedEdge = getSharedEdge(first, second)
  val middle = sharedEdge.middle
  val firstOuterEdge = getOppositeQuadEdge(first, sharedEdge)
  val firstVector = (firstOuterEdge.middle - middle).normalize()
  return firstVector.dot(second.normal) > 0
}

fun getIncompleteNeighbors(face: FlexibleFace): Sequence<FlexibleFace> =
    face.neighbors
        .asSequence()
        .filter {
          val info = getFaceInfo(it)
          info.type == FaceType.wall && faceNodeCount(info) == 1
        }

fun getConcaveCorners(face: FlexibleFace): Sequence<FlexibleFace> =
    getIncompleteNeighbors(face)
        .filter { isConcaveCorner(face, it) }

fun traceIncompleteWalls(origin: FlexibleFace, first: FlexibleFace, otherEnd: FlexibleFace): Pair<MutableList<FlexibleFace>, List<FlexibleFace>> {
  var current = first
  var previous = origin
  val collected = mutableListOf(first)
//  val notUsedResult = mutableListOf<FlexibleFace>()
  var step = 0
  while (true) {
    val neighbors = getIncompleteNeighbors(current).filter { it != previous }.toList()
    val n = neighbors.filter { !isConcaveCorner(current, it) }.toList()
//    assert(neighbors.any())
    assert(n.size < 2)
    val next = n.firstOrNull()
    if (next == null) {
      val notUsed = neighbors.toList()
      assert(notUsed.size < 2)
      return Pair(collected, notUsed)
    }
    if (next == otherEnd) {
      return Pair(collected, listOf())
    }
    collected.add(next)
    previous = current
    current = next
    ++step
  }
}

//fun getJoiningPoint(first: FlexibleFace, second: FlexibleFace): Vector3 =
//    first.edges.intersect(second.edges).first().first

//fun getJoiningPoint(a: Collection<EdgeReference>, b: Collection<EdgeReference>): Vector3 =
//    a.intersect(b).first().first

fun getEndEdge(walls: List<FlexibleFace>, offset: Int): FlexibleEdge {
  val head = walls.size - 1 - offset
  val last = walls[head]
  val nextLast = walls[head - 1]
  return verticalEdges(last).first { e -> nextLast.edges.none { it.edge == e.edge } }.edge
//  return last.edges.intersect(nextLast.edges).first().edge
}

fun getEndEdgeReversed(walls: List<FlexibleFace>, offset: Int): FlexibleEdge {
  val head = 0 + offset
  val last = walls[head]
  val nextLast = walls[head + 1]
  return verticalEdges(last).first { e -> nextLast.edges.none { it.edge == e.edge } }.edge
}

fun getEndPoint(walls: List<FlexibleEdge>, offset: Int): Vector3 {
  val head = walls.size - 1 - offset
  val last = walls[head]
  val nextLast = walls[head - 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun getEndPointReversed(walls: List<FlexibleEdge>, offset: Int): Vector3 {
  val head = 0 + offset
  val last = walls[head]
  val nextLast = walls[head + 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun shaveOffOccludedWalls(a: Vector3, walls: List<FlexibleEdge>, notUsed: List<FlexibleEdge>, shaveCount: Int = 0): Int {
  // 3 is an estimate right now.  A sector needs at least 3 walls but this condition may not directly translate to wall count.
//  assert(shaveCount < walls.size - 2)
  if (shaveCount >= walls.size - 2)
    return shaveCount

  val b = getEndPoint(walls, shaveCount)
  val e = 0.001f
  return if (notUsed.none {
        val (hit, point) = lineSegmentIntersectsLineSegment(a, b, it.first, it.second)
        hit && (point == null || (point.distance(a.xy()) > e && point.distance(b.xy()) > e))
      })
    shaveCount
  else
    shaveOffOccludedWalls(a, walls, notUsed, shaveCount + 1)
}

fun chainIntegrity(walls: List<FlexibleFace>): List<Int> =
    walls.drop(1).zip(walls.dropLast(1)) { a, b ->
      a.edges.count { e -> b.edges.any { it.edge == e.edge } }
    }

fun isChain(walls: List<FlexibleFace>): Boolean =
    chainIntegrity(walls)
        .all { it == 1 }

fun gatherNewSectorFaces(origin: FlexibleFace): List<FlexibleFace> {
  val firstNeighbors = getIncompleteNeighbors(origin).filter { !isConcaveCorner(origin, it) }.toList()
  assert(firstNeighbors.size == 2)
  val (firstDir, firstNotUsed) = traceIncompleteWalls(origin, firstNeighbors[0], origin)
  val (secondDir, secondNotUsed) = traceIncompleteWalls(origin, firstNeighbors[1], firstDir.last())
  val notUsed = if (firstNotUsed.none())
    listOf()
  else
    firstNotUsed.plus(secondNotUsed).distinct()

  val walls = firstDir.reversed().plus(origin).plus(secondDir)
  assert(walls.size > 1)
  val i = chainIntegrity(walls)
  assert(isChain(walls))

  return if (notUsed.any()) {
//    assert(firstNotUsed.any() && secondNotUsed.any())
    val edges = walls.map { getFloor(it).edge }
    val a = getEndPointReversed(edges, 0)
    val unusedEdges = notUsed.map { getFloor(it).edge }
    val shaveCount = shaveOffOccludedWalls(a, edges, unusedEdges)
//    println(shaveCount)
//    val c = getEndEdgeReversed(walls.dropLast(shaveCount), 0)
//    val d = getEndEdge(walls.dropLast(shaveCount), 0)
    val count = walls.size - shaveCount
//    assert(count > 2 || (count > 1 && c != d))
//    println("size " + count + ", " + shaveCount)

    walls.dropLast(shaveCount)
  } else {
    assert(walls.size > 2)
//    println("size " + walls.size)
    walls
  }
}

fun getDistinctEdges(edges: Edges) =
    edges.distinctBy { it.vertices.map { it.hashCode() }.sorted() }

fun addSpaceNode(abstractWorld: AbstractWorld, node: Node) {
  abstractWorld.graph.nodes.add(node)
  node.walls
      .mapNotNull { getOtherNode(node, it) }
      .forEach {
        abstractWorld.graph.connect(node, it, ConnectionType.obstacle)
      }
}

fun createSpaceNode(sectorCenter: Vector3, abstractWorld: AbstractWorld, biome: Biome, dice: Dice): Node {
  val isSolid = if (!biome.hasEnclosedRooms || dice.getInt(0, 1) == 0)
    false
  else
    true

  return createSecondaryNode(sectorCenter, abstractWorld, isSolid, biome)
}

fun addSpaceNode(abstractWorld: AbstractWorld, originFace: FlexibleFace, dice: Dice) {
  val walls = gatherNewSectorFaces(originFace)
  val a = getEndEdgeReversed(walls, 0)
  val b = getEndEdge(walls, 0)

  assert(walls.size > 2 || (walls.size > 1 && a != b))

  val edges = walls.flatMap { face ->
    face.edges.filter { edge ->
      edge.first.z != edge.second.z
    }.map { it.edge }
  }.distinct()

  val floorVertices = edges.map { it.vertices.sortedBy { it.z }.last() }
  val ceilingVertices = edges.map { it.vertices.sortedBy { it.z }.first() }
  val sectorCenter = getCenter(floorVertices)
  val flatCenter = sectorCenter.xy()

  val node = createSpaceNode(sectorCenter, abstractWorld, getFaceInfo(originFace).firstNode!!.biome, dice)
  node.walls.addAll(walls)
  walls.forEach {
    val info = getFaceInfo(it)
    info.secondNode = node
  }

  val floor = createFloor(abstractWorld.mesh, node, floorVertices, flatCenter)
  val ceiling = createCeiling(abstractWorld.mesh, node, ceilingVertices, flatCenter)

  val gapEdges = edges.filter {
    it.faces.count { walls.contains(it) } < 2
  }

  if (a != b) {
//    assert(gapEdges.size == 2)
    val d = walls.map { getFaceInfo(it).firstNode!!.index }
    val gapVertices = getNewWallVertices(sectorCenter, listOf(a, b))
    val newWall = abstractWorld.mesh.createStitchedFace(gapVertices)
    if (node.isSolid)
      newWall.flipQuad()

    newWall.data = FaceInfo(FaceType.wall, node, null)
    node.walls.add(newWall)
  }

  addSpaceNode(abstractWorld, node)
}

fun getIncomplete(abstractWorld: AbstractWorld) =
    abstractWorld.graph.nodes.flatMap { it.walls }
        .filter { faceNodeCount(getFaceInfo(it)) == 1 }

fun createBoundarySector(abstractWorld: AbstractWorld, originFace: FlexibleFace, dice: Dice) {
  getFaceInfo(originFace).debugInfo = "space-d"
  val originalWall = getWallVertices(originFace.vertices)

  val newPoints = originFace.vertices.map {
    val projected = it.xy() + it.xy().normalize() * 10f
    Vector3(projected.x, projected.y, it.z)
  }
  val newWall = getWallVertices(newPoints)

  val floorVertices = originalWall.upper.plus(newWall.upper)
//  val ceilingVertices = originalWall.lower.plus(newWall.lower)
  val sectorCenter = getCenter(floorVertices)

  val node = createSpaceNode(sectorCenter, abstractWorld, getFaceInfo(originFace).firstNode!!.biome, dice)

//  node.walls.addAll(walls)
//  node.floors.add(floor)
  val floor = createFloor(abstractWorld.mesh, node, floorVertices, sectorCenter.xy())
  val ceiling = createCeiling(abstractWorld.mesh, node, floorVertices, sectorCenter.xy())
//  initializeFaceInfo(FaceType.wall, node, floor, Textures.ground)
  node.walls.add(originFace)
  getFaceInfo(originFace).secondNode = node
  val outerWall = createWall(abstractWorld, node, newPoints)

  for (i in 0..1) {
    val outerSideEdge = outerWall.edge(newWall.lower[i], newWall.upper[1 - i])
    assert(outerSideEdge != null)
    val neighborWalls = outerSideEdge!!.faces.filter { getFaceInfo(it).type == FaceType.wall }
    if (neighborWalls.size > 1) {
      val missingWalls = neighborWalls.filter { !node.walls.contains(it) && it.edges.any { originFace.edges.map { it.edge }.contains(it.edge) } }
      node.walls.addAll(missingWalls)
      missingWalls.forEach { getFaceInfo(it).secondNode = node }
      continue
    }

    val sidePoints = listOf(
        originalWall.lower[i],
        newWall.lower[i],
        newWall.upper[1 - i],
        originalWall.upper[1 - i]
    )
    createWall(abstractWorld, node, sidePoints)
  }

//  initializeNodeFaceInfo(node, null, null)
  addSpaceNode(abstractWorld, node)
}

fun fillBoundary(abstractWorld: AbstractWorld, dice: Dice) {
  val faces = getIncomplete(abstractWorld)
  for (face in faces) {
    createBoundarySector(abstractWorld, face, dice)
  }
}

fun defineNegativeSpace(abstractWorld: AbstractWorld, dice: Dice) {
  var pass = 1
  while (true) {
    val faces = getIncomplete(abstractWorld)

    val neighborLists = faces.map { wall -> Pair(wall, getIncompleteNeighbors(wall).toList()) }
    val invalid = neighborLists.filter { it.second.size > 2 }
//    assert(invalid.none())
    if (invalid.any())
      return

    assert(invalid.none())
//  processIncompleteEdges(edges)
    val concaveFaces = faces
        .filter { wall ->
          val neighbors = getIncompleteNeighbors(wall).toList()
          neighbors.size > 1 && neighbors.all { !isConcaveCorner(wall, it) }
        }

    concaveFaces.forEach {
      getFaceInfo(it).debugInfo = "space-a"
    }

    val convexFaces = faces
        .filter { wall ->
          val neighbors = getIncompleteNeighbors(wall).toList()
          neighbors.size > 1 && neighbors.all { isConcaveCorner(wall, it) }
        }

    convexFaces.forEach {
      getFaceInfo(it).debugInfo = "space-b"
    }

    if (concaveFaces.none()) {
      faces
          .filter { wall ->
            val neighbors = getIncompleteNeighbors(wall).toList()
            neighbors.size < 2
          }.forEach {
            //        getFaceInfo(it).debugInfo = "space-d"
          }
      break
    }
    for (originFace in concaveFaces) {
      if (faceNodeCount(originFace) == 1) {
        val walls = gatherNewSectorFaces(originFace)
        if (walls.size < 2) {
          val i = getIncompleteNeighbors(originFace).toList()
          getFaceInfo(originFace).debugInfo = "space-d"
          return
        }
        addSpaceNode(abstractWorld, originFace, dice)
      }
    }
    ++pass
  }
}