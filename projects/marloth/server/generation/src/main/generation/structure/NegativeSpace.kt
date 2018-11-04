package generation.structure

import mythic.ent.Id
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.plus
import physics.voidNodeId
import randomly.Dice
import simulation.FaceType
import mythic.ent.IdSource
import mythic.ent.entityMap
import simulation.getFloor
import simulation.*

fun zeroIfVoid(value: Id) =
    if (value == voidNodeId)
      0
    else
      1

fun faceNodeCount(faceInfo: ConnectionFace) =
    zeroIfVoid(faceInfo.firstNode) + zeroIfVoid(faceInfo.secondNode)

fun faceNodeCount(faces: ConnectionTable, face: ImmutableFace) =
    faceNodeCount(faces[face.id]!!)

//fun getNextFace(face: ImmutableFace): ImmutableFace? {
//  return face.edges.filter()
//  val info = getFaceInfo(face)
//  val node = info.firstNode!!
//  return node.walls.filter { it != face && it.vertices.union(face.vertices).size >= 2 }.firstOrNull()
//}

fun getSharedEdge(first: ImmutableFace, second: ImmutableFace): ImmutableEdge =
    first.edges.first { edge -> second.edges.map { it.edge }.contains(edge.edge) }.edge

// This algorithm only works on quads
fun getOppositeQuadEdge(first: ImmutableFace, edge: ImmutableEdge) =
    first.edges.first { e -> e.vertices.none { edge.vertices.contains(it) } }

fun isConcaveCorner(first: ImmutableFace, second: ImmutableFace): Boolean {
  val sharedEdge = getSharedEdge(first, second)
  val middle = sharedEdge.middle
  val firstOuterEdge = getOppositeQuadEdge(first, sharedEdge)
  val firstVector = (firstOuterEdge.middle - middle).normalize()
  return firstVector.dot(second.normal) > 0
}

fun isConcaveCorner(a: Vector3, b: Vector3, bcNormal: Vector3): Boolean {
  val firstVector = (a - b).normalize()
  return firstVector.dot(bcNormal) > 0
}

fun getIncompleteNeighbors(faces: ConnectionTable, face: ImmutableFace): Sequence<ImmutableFace> =
    face.neighbors
        .asSequence()
        .filter {
          val info = faces[it.id]!!
          info.faceType == FaceType.wall && faceNodeCount(info) == 1
        }

//fun getConcaveCorners(face: ImmutableFace): Sequence<ImmutableFace> =
//    getIncompleteNeighbors(face)
//        .filter { isConcaveCorner(face, it) }

fun traceIncompleteWalls(faces: ConnectionTable, origin: ImmutableFace, first: ImmutableFace, otherEnd: ImmutableFace): Pair<MutableList<ImmutableFace>, List<ImmutableFace>> {
  var current = first
  var previous = origin
  val collected = mutableListOf(first)
//  val notUsedResult = mutableListOf<ImmutableFace>()
  var step = 0
  while (true) {
    val neighbors = getIncompleteNeighbors(faces, current).filter { it != previous }.toList()
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

//fun getJoiningPoint(first: ImmutableFace, second: ImmutableFace): Vector3 =
//    first.edges.intersect(second.edges).first().first

//fun getJoiningPoint(a: Collection<ImmutableEdgeReference>, b: Collection<ImmutableEdgeReference>): Vector3 =
//    a.intersect(b).first().first

fun getEndEdge(walls: List<ImmutableFace>, offset: Int): ImmutableEdge {
  val head = walls.size - 1 - offset
  val last = walls[head]
  val nextLast = walls[head - 1]
  return verticalEdges(last).first { e -> nextLast.edges.none { it.edge == e.edge } }.edge
//  return last.edges.intersect(nextLast.edges).first().edge
}

fun getEndEdgeReversed(walls: List<ImmutableFace>, offset: Int): ImmutableEdge {
  val head = 0 + offset
  val last = walls[head]
  val nextLast = walls[head + 1]
  return verticalEdges(last).first { e -> nextLast.edges.none { it.edge == e.edge } }.edge
}

fun getEndPoint(walls: List<ImmutableEdge>, offset: Int): Vector3 {
  val head = walls.size - 1 - offset
  val last = walls[head]
  val nextLast = walls[head - 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun getEndPointReversed(walls: List<ImmutableEdge>, offset: Int): Vector3 {
  val head = 0 + offset
  val last = walls[head]
  val nextLast = walls[head + 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun shaveOffOccludedWalls(points: List<Vector3>, walls: List<ImmutableFace>, shaveCount: Int = 0): Int {
  // 3 is an estimate right now.  A sector needs at least 3 walls but this condition may not directly translate to wall count.
//  assert(shaveCount < walls.size - 2)
  if (shaveCount >= points.size - 2)
    return shaveCount

  if (_i == 3) {
    val k = 0
  }
//  val a2 = points[1]
  val a = points[0]
  val b = points[points.size - shaveCount - 1]
//  val b2 = points[points.size - shaveCount - 1]
  val firstNormal = walls[0].normal
  val secondNormal = walls[walls.size - shaveCount - 1].normal
  val e = 0.001f
  return if (!isConcaveCorner(a, b, secondNormal) && !isConcaveCorner(b, a, firstNormal))
    shaveCount
  else
    shaveOffOccludedWalls(points, walls, shaveCount + 1)
}

fun chainIntegrity(walls: List<ImmutableFace>): List<Int> =
    walls.drop(1).zip(walls.dropLast(1)) { a, b ->
      a.edges.count { e -> b.edges.any { it.edge == e.edge } }
    }

fun isChain(walls: List<ImmutableFace>): Boolean =
    chainIntegrity(walls)
        .all { it == 1 }

fun lineChainToVertexChain(edges: List<ImmutableEdge>): List<Vector3> =
    listOf(edges[0].vertices.first { !edges[1].vertices.contains(it) })
        .plus(edges.drop(1).zip(edges.dropLast(1)) { c, d -> c.vertices.intersect(d.vertices).first() })
        .plus(edges.last().vertices.first { !edges[edges.size - 2].vertices.contains(it) })

var _i: Int = 0
fun gatherNewSectorFaces(faces: ConnectionTable, origin: ImmutableFace): List<ImmutableFace> {
  ++_i
  val firstNeighbors = getIncompleteNeighbors(faces, origin).filter { !isConcaveCorner(origin, it) }.toList()
  assert(firstNeighbors.size == 2)
  val (firstDir, firstNotUsed) = traceIncompleteWalls(faces, origin, firstNeighbors[0], origin)
  val (secondDir, secondNotUsed) = traceIncompleteWalls(faces, origin, firstNeighbors[1], firstDir.last())
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
    notUsed.forEach { faces[it.id]!!.debugInfo = "space-c" }
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

//fun addSpaceNode2(nodes: List<Node>, connections: InitialConnections, faces: ConnectionTable, node: Node): Pair<List<Node>, InitialConnections> {
//  return Pair(
//      nodes.plus(node),
//      connections.plus(
//          node.walls
//              .mapNotNull { getOtherNode(node, faces[it.id]!!) }
//              .map {
//                InitialConnection(node.id, it, ConnectionType.obstacle, FaceType.wall)
//              })
//  )
//}

fun createSpaceNode(sectorCenter: Vector3, nextId: IdSource): Node {
//  val isSolid = when(biome.enclosure){
//    Enclosure.all -> true
//    Enclosure.none -> false
//    Enclosure.some -> dice.getInt(0, 3) > 0
//  }

  return createSecondaryNode(sectorCenter, nextId, true)
}

fun addSpaceNode(idSources: StructureIdSources, realm: StructureRealm, originFace: ImmutableFace, dice: Dice): StructureRealm {
  val walls = gatherNewSectorFaces(realm.connections, originFace)
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

  val node = createSpaceNode(sectorCenter, idSources.node)
  node.walls.addAll(walls)
  val updatedWalls = walls.map {
    val info = realm.connections[it.id]!!
    info.copy(secondNode = node.id)
  }

  val floor = createFloor(idSources, realm.mesh, node, floorVertices, flatCenter)
  val ceiling = createCeiling(idSources, realm.mesh, node, ceilingVertices, flatCenter)

  val gapEdges = edges.filter { edge ->
    edge.faces.count { walls.contains(it) } < 2
  }

  val gapWall = if (a != b) {
    val gapVertices = getNewWallVertices(sectorCenter, listOf(a, b))
    val newWall = realm.mesh.createStitchedFace(idSources.edge, idSources.face(), gapVertices)
    if (node.isSolid)
      newWall.flipQuad()

    //    newWall.data = ConnectionFace(FaceType.wall, node, null, null, "space-b")
    node.walls.add(newWall)
    val connection = ConnectionFace(newWall.id, FaceType.wall, node.id, voidNodeId, null, "space-b")
    listOf(FacePair(connection, newWall))
  } else
    listOf()

  val (newConnections, newFaces) = splitFacePairTables(
      listOf(floor, ceiling)
          .plus(gapWall)
  )

  val updatedConnections = newConnections
      .plus(entityMap(updatedWalls))

  return realm.copy(
      nodes = realm.nodes.plus(Pair(node.id, node)),
      connections = realm.connections.plus(updatedConnections),
      mesh = realm.mesh.copy(
          faces = realm.mesh.faces.plus(newFaces)
      )
  )
}

fun getIncomplete(faces: ConnectionTable, nodes: Collection<Node>) =
    nodes.flatMap { it.walls }
        .filter { faceNodeCount(faces[it.id]!!) == 1 }

fun createBoundarySector(idSources: StructureIdSources, realm: StructureRealm, originFace: ImmutableFace, dice: Dice): StructureRealm {
  val originalWall = getWallVertices(originFace.vertices)

  val newPoints = originFace.vertices.map {
    val projected = it.xy() + it.xy().normalize() * 10f
    Vector3(projected.x, projected.y, it.z)
  }
  val newWall = getWallVertices(newPoints)
  val floorVertices = originalWall.upper.plus(newWall.upper)
  val sectorCenter = getCenter(floorVertices)

  val node = createSpaceNode(sectorCenter, idSources.node)

  val floor = createFloor(idSources, realm.mesh, node, floorVertices, sectorCenter.xy())
  val ceiling = createCeiling(idSources, realm.mesh, node, floorVertices, sectorCenter.xy())
//  initializeFaceInfo(FaceType.wall, node, floor, Textures.ground)
  node.walls.add(originFace)
  val updatedOriginFace = realm.connections[originFace.id]!!.copy(secondNode = node.id)
  val outerWall = createWall(idSources, realm.mesh, node, newPoints)
//  mesh.faces[outerWall.geometry.id]!!.debugInfo = "space-d"
  val faces1 = listOf(outerWall)
      .plus(floor)
      .plus(ceiling)
      .plus(FacePair(updatedOriginFace, originFace))

  val faceTable = realm.connections.plus(entityMap(faces1.map { it.info }))

  val missingWallsAccumulator = mutableListOf<FacePair>()
  val sideWalls = (0..1).filter { i ->
    val outerSideEdge = outerWall.geometry.edge(newWall.lower[i], newWall.upper[1 - i])
    assert(outerSideEdge != null)
    val neighborWalls = outerSideEdge!!.faces.filter { faceTable[it.id]!!.faceType == FaceType.wall }
    if (neighborWalls.size > 1) {
      val missingWalls = neighborWalls.filter { !node.walls.contains(it) && it.edges.any { originFace.edges.map { it.edge }.contains(it.edge) } }
      node.walls.addAll(missingWalls)
      missingWallsAccumulator.plus(missingWalls.associate { Pair(it.id, faceTable[it.id]!!.copy(secondNode = node.id)) })

      false
    } else
      true
  }.map { i ->
    val sidePoints = listOf(
        originalWall.lower[i],
        newWall.lower[i],
        newWall.upper[1 - i],
        originalWall.upper[1 - i]
    )
    val wall = createWall(idSources, realm.mesh, node, sidePoints)
//    mesh.faces[wall.geometry.id]!!.debugInfo = "space-d"
    wall
  }

  val (connections2, faces2) = splitFacePairTables(faces1.plus(sideWalls.plus(missingWallsAccumulator)))

  return StructureRealm(
      nodes = realm.nodes.plus(Pair(node.id, node)),
      connections = realm.connections.plus(connections2),
      mesh = realm.mesh.copy(
          faces = realm.mesh.faces.plus(faces2)
      )
  )
//  return Pair(addSpaceNode2(realm.graph, connections2, node), connections2)
}

fun fillBoundary(idSources: StructureIdSources, realm: StructureRealm, dice: Dice): StructureRealm {
  var currentRealm = realm
  val incompleteFaces = getIncomplete(realm.connections, realm.nodes.values)
  for (face in incompleteFaces) {
    val result = createBoundarySector(idSources, currentRealm, face, dice)
    currentRealm = currentRealm.copy(
        nodes = result.nodes,
        connections = result.connections,
        mesh = result.mesh
    )
  }

  return currentRealm
}

data class StructureIdSources(
    val node: IdSource,
    override val face: IdSource,
    override val edge: IdSource
) : GeometryIdSources

fun defineNegativeSpace(idSources: StructureIdSources, realm: StructureRealm, dice: Dice): StructureRealm {
  var pass = 1
  var currentRealm = realm
  while (true) {
    val faces = getIncomplete(currentRealm.connections, currentRealm.nodes.values)

    val neighborLists = faces.map { wall -> Pair(wall, getIncompleteNeighbors(currentRealm.connections, wall).toList()) }
    val invalid = neighborLists.filter { it.second.size > 2 }
//    assert(invalid.none())
    if (invalid.any())
      return currentRealm

    assert(invalid.none())
    val concaveFaces = faces
        .filter { wall ->
          val neighbors = getIncompleteNeighbors(currentRealm.connections, wall).toList()
          neighbors.size > 1 && neighbors.all { !isConcaveCorner(wall, it) }
        }

    val convexFaces = faces
        .filter { wall ->
          val neighbors = getIncompleteNeighbors(currentRealm.connections, wall).toList()
          neighbors.size > 1 && neighbors.all { isConcaveCorner(wall, it) }
        }

    if (concaveFaces.none()) {
      faces
          .filter { wall ->
            val neighbors = getIncompleteNeighbors(currentRealm.connections, wall).toList()
            neighbors.size < 2
          }.forEach {
            //        getFaceInfo(it).debugInfo = "space-d"
          }
      break
    }
    for (originFace in concaveFaces) {
      if (faceNodeCount(currentRealm.connections, originFace) == 1) {
        val walls = gatherNewSectorFaces(currentRealm.connections, originFace)
        if (walls.size < 2) {
          val i = getIncompleteNeighbors(currentRealm.connections, originFace).toList()
          return currentRealm
        }
        currentRealm = addSpaceNode(idSources, currentRealm, originFace, dice)
      }
    }
    ++pass
  }

  return currentRealm
}