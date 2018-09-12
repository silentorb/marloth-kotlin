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
    first.edges.first { e-> e.vertices.none { edge.vertices.contains(it) } }

fun isConcaveCorner(first: FlexibleFace, second: FlexibleFace): Boolean {
  val sharedEdge = getSharedEdge(first, second)
  val middle = sharedEdge.middle
  val firstOuterEdge = getOppositeQuadEdge(first, sharedEdge)
  val firstVector = Vector3((firstOuterEdge.middle - middle).normalize())
  return firstVector.dot(second.normal) > 0
}

fun isConcaveCorner(a: Vector3, b: Vector3, bcNormal: Vector3): Boolean {
  val firstVector = (a - b).normalize()
  return firstVector.dot(bcNormal) > 0
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

//fun getJoiningPoint(first: FlexibleFace, second: FlexibleFace): Vector3m =
//    first.edges.intersect(second.edges).first().first

//fun getJoiningPoint(a: Collection<EdgeReference>, b: Collection<EdgeReference>): Vector3m =
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

fun getEndPoint(walls: List<FlexibleEdge>, offset: Int): Vector3m {
  val head = walls.size - 1 - offset
  val last = walls[head]
  val nextLast = walls[head - 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun getEndPointReversed(walls: List<FlexibleEdge>, offset: Int): Vector3m {
  val head = 0 + offset
  val last = walls[head]
  val nextLast = walls[head + 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun shaveOffOccludedWalls(points: List<Vector3m>, walls: List<FlexibleFace>, shaveCount: Int = 0): Int {
  // 3 is an estimate right now.  A sector needs at least 3 walls but this condition may not directly translate to wall count.
//  assert(shaveCount < walls.size - 2)
  if (shaveCount >= points.size - 2)
    return shaveCount

  if (_i == 3) {
    val k = 0
  }
//  val a2 = points[1]
  val a = Vector3(points[0])
  val b = Vector3(points[points.size - shaveCount - 1])
//  val b2 = points[points.size - shaveCount - 1]
  val firstNormal = walls[0].normal
  val secondNormal = walls[walls.size - shaveCount - 1].normal
  val e = 0.001f
  return if (!isConcaveCorner(a, b, secondNormal) && !isConcaveCorner(b, a, firstNormal))
    shaveCount
  else
    shaveOffOccludedWalls(points, walls, shaveCount + 1)
}

fun chainIntegrity(walls: List<FlexibleFace>): List<Int> =
    walls.drop(1).zip(walls.dropLast(1)) { a, b ->
      a.edges.count { e -> b.edges.any { it.edge == e.edge } }
    }

fun isChain(walls: List<FlexibleFace>): Boolean =
    chainIntegrity(walls)
        .all { it == 1 }

fun lineChainToVertexChain(edges: List<FlexibleEdge>): List<Vector3m> =
    listOf(edges[0].vertices.first { !edges[1].vertices.contains(it) })
        .plus(edges.drop(1).zip(edges.dropLast(1)) { c, d -> c.vertices.intersect(d.vertices).first() })
        .plus(edges.last().vertices.first { !edges[edges.size - 2].vertices.contains(it) })

var _i: Int = 0
fun gatherNewSectorFaces(origin: FlexibleFace): List<FlexibleFace> {
  ++_i
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
  assert(isChain(walls))

  return if (notUsed.any()) {
//    assert(firstNotUsed.any() && secondNotUsed.any())
    val edges = walls.map { getFloor(it).edge }
//    val a = getEndPointReversed(edges, 0)
    val unusedEdges = notUsed.map { getFloor(it).edge }
    notUsed.forEach { getFaceInfo(it).debugInfo = "space-c" }
    val points = lineChainToVertexChain(edges)

    val shaveCount = shaveOffOccludedWalls(points, walls)
//    println(shaveCount)
//    val c = getEndEdgeReversed(walls.dropLast(shaveCount), 0)
//    val d = getEndEdge(walls.dropLast(shaveCount), 0)
    val count = walls.size - shaveCount
//    assert(count > 2 || (count > 1 && c != d))
//    println("size " + count + ", " + shaveCount)

    if (shaveCount > 0) {
      val k = 0
    }
    walls.dropLast(shaveCount)
  } else {
    assert(walls.size > 2)
//    println("size " + walls.size)
    walls
  }
}

fun getDistinctEdges(edges: Edges) =
    edges.distinctBy { it.vertices.map { it.hashCode() }.sorted() }

fun addSpaceNode(realm: Realm, node: Node) {
  realm.graph.nodes.add(node)
  node.walls
      .mapNotNull { getOtherNode(node, it) }
      .forEach {
        realm.graph.connect(node, it, ConnectionType.obstacle)
      }
}

fun createSpaceNode(sectorCenter: Vector3m, realm: Realm, biome: Biome, dice: Dice): Node {
  val isSolid = when(biome.enclosure){
    Enclosure.all -> true
    Enclosure.none -> false
    Enclosure.some -> dice.getInt(0, 3) > 0
  }

  return createSecondaryNode(sectorCenter, realm, isSolid, biome)
}

fun addSpaceNode(realm: Realm, originFace: FlexibleFace, dice: Dice) {
  val walls = gatherNewSectorFaces(originFace)
  val a = getEndEdgeReversed(walls, 0)
  val b = getEndEdge(walls, 0)

  assert(walls.size > 2 || (walls.size > 1 && a != b))

  val edges = walls.flatMap { face ->
    face.edges.filter { edge ->
      edge.first.z != edge.second.z
    }.map { it.edge }
  }.distinct()

  val floorVertices = edges.map { edge -> edge.vertices.sortedBy { it.z }.first() }
  val ceilingVertices = edges.map { edge -> edge.vertices.sortedBy { it.z }.last() }
  val sectorCenter = getCenter(floorVertices)
  val flatCenter = sectorCenter.xy()

  val node = createSpaceNode(sectorCenter, realm, getFaceInfo(originFace).firstNode!!.biome, dice)
  node.walls.addAll(walls)
  walls.forEach {
    val info = getFaceInfo(it)
    info.secondNode = node
  }

  val floor = createFloor(realm.mesh, node, floorVertices, flatCenter)
  val ceiling = createCeiling(realm.mesh, node, ceilingVertices, flatCenter)

  val gapEdges = edges.filter {
    it.faces.count { walls.contains(it) } < 2
  }

  if (a != b) {
//    assert(gapEdges.size == 2)
    val d = walls.map { getFaceInfo(it).firstNode!!.index }
    val gapVertices = getNewWallVertices(sectorCenter, listOf(a, b))
    val newWall = realm.mesh.createStitchedFace(gapVertices)
    if (node.isSolid)
      newWall.flipQuad()

    newWall.data = FaceInfo(FaceType.wall, node, null, null, "space-b")
    node.walls.add(newWall)
  }

  addSpaceNode(realm, node)
}

fun getIncomplete(realm: Realm) =
    realm.graph.nodes.flatMap { it.walls }
        .filter { faceNodeCount(getFaceInfo(it)) == 1 }

fun createBoundarySector(realm: Realm, originFace: FlexibleFace, dice: Dice) {
  val originalWall = getWallVertices(originFace.vertices)

  val newPoints = originFace.vertices.map {
    val projected = it.xy() + it.xy().normalize() * 10f
    Vector3m(projected.x, projected.y, it.z)
  }
  val newWall = getWallVertices(newPoints)
  val floorVertices = originalWall.upper.plus(newWall.upper)
//  val ceilingVertices = originalWall.lower.plus(newWall.lower)
  val sectorCenter = getCenter(floorVertices)

  val node = createSpaceNode(sectorCenter, realm, getFaceInfo(originFace).firstNode!!.biome, dice)

//  node.walls.addAll(walls)
//  node.floors.add(floor)
  val floor = createFloor(realm.mesh, node, floorVertices, sectorCenter.xy())
  val ceiling = createCeiling(realm.mesh, node, floorVertices, sectorCenter.xy())
//  initializeFaceInfo(FaceType.wall, node, floor, Textures.ground)
  node.walls.add(originFace)
  getFaceInfo(originFace).secondNode = node
  val outerWall = createWall(realm, node, newPoints)
  getFaceInfo(outerWall).debugInfo = "space-d"

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
    val wall = createWall(realm, node, sidePoints)
    getFaceInfo(wall).debugInfo = "space-d"
  }

//  initializeNodeFaceInfo(node, null, null)
  addSpaceNode(realm, node)
}

fun fillBoundary(realm: Realm, dice: Dice) {
  val faces = getIncomplete(realm)
  for (face in faces) {
    createBoundarySector(realm, face, dice)
  }
}

fun defineNegativeSpace(realm: Realm, dice: Dice) {
  var pass = 1
  while (true) {
    val faces = getIncomplete(realm)

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

    val convexFaces = faces
        .filter { wall ->
          val neighbors = getIncompleteNeighbors(wall).toList()
          neighbors.size > 1 && neighbors.all { isConcaveCorner(wall, it) }
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
          return
        }
        addSpaceNode(realm, originFace, dice)
      }
    }
    ++pass
  }
}