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

fun isIncompleteWall(faces: ConnectionTable): (Id) -> Boolean = { id ->
  val info = faces[id]!!
  val c = faceNodeCount(info) == 1
  info.faceType == FaceType.wall && c
}

fun getIncompleteNeighbors(faces: ConnectionTable, face: ImmutableFace): Collection<ImmutableFace> =
    face.neighbors
        .filter { isIncompleteWall(faces)(it.id) }

data class WallTraceResult(
    val used: List<ImmutableFace>,
    val notUsed: List<ImmutableFace>
)

fun traceIncompleteWalls(faces: ConnectionTable, origin: ImmutableFace, first: ImmutableFace, otherEnd: ImmutableFace): List<ImmutableFace> {
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
      return collected
    }
    if (next == otherEnd) {
      return collected
    }
    collected.add(next)
    previous = current
    current = next
    ++step
  }
}

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

fun getEndPoint(first: ImmutableEdge, second: ImmutableEdge): Vector3 {
  return second.vertices.first { !first.vertices.contains(it) }
}

fun getEndPointReversed(walls: List<ImmutableEdge>, offset: Int): Vector3 {
  val head = 0 + offset
  val last = walls[head]
  val nextLast = walls[head + 1]
  return last.vertices.first { !nextLast.vertices.contains(it) }
}

fun shaveOffOccludedWallsOld(points: List<Vector3>, walls: List<ImmutableFace>, shaveCount: Int = 0): Int {
  // 3 is an estimate right now.  A sector needs at least 3 walls but this condition may not directly translate to wall count.
//  assert(shaveCount < walls.size - 2)
  if (shaveCount >= points.size - 2)
    return shaveCount

  val a = points[0]
  val b = points[points.size - shaveCount - 1]
  val firstNormal = walls[0].normal
  val secondNormal = walls[walls.size - shaveCount - 1].normal
  return if (!isConcaveCorner(a, b, secondNormal) && !isConcaveCorner(b, a, firstNormal))
    shaveCount
  else
    shaveOffOccludedWallsOld(points, walls, shaveCount + 1)
}

fun chainIntegrity(walls: List<ImmutableFace>): List<Int> =
    walls.drop(1).zip(walls.dropLast(1)) { a, b ->
      a.edges.count { e -> b.edges.any { it.edge == e.edge } }
    }

fun isChain(walls: List<ImmutableFace>): Boolean =
    chainIntegrity(walls)
        .all { it == 1 }

val lineChainToVertexChain: (List<ImmutableEdge>) -> List<Vector3> = { edges ->
  listOf(edges[0].vertices.first { !edges[1].vertices.contains(it) })
      .plus(edges.drop(1).zip(edges.dropLast(1)) { c, d -> c.vertices.intersect(d.vertices).first() })
      .plus(edges.last().vertices.first { !edges[edges.size - 2].vertices.contains(it) })
}

fun intersects(faces: ImmutableFaceTable, group: List<Id>, points: List<Vector3>): Boolean = group
    .any { id ->
      if (id == 436L) {
        val k = 0
      }
      val face = faces[id]!!
      val floor = getFloor(face)
      val (hit, _) = lineSegmentIntersectsLineSegment(points[0], points[1], floor.first, floor.second)
      if (hit) {
        val k = 0
        println("Hit " + id)
      }
      hit
    }

fun shaveOccluded(faces: ImmutableFaceTable, group: List<Id>, origin: ImmutableFace, arms: List<List<ImmutableFace>>): List<Int>? {
  if (arms.all { it.isEmpty() })
    return null

  val originFloor = getFloor(origin).edge
  val edgeArms = arms.map { arm -> arm.map { getFloor(it).edge } }

  val chains = edgeArms
      .zip(edgeArms.reversed()) { a, b -> listOfNotNull(b.firstOrNull(), originFloor).plus(a) }
      .map(lineChainToVertexChain)
      .map { it.drop(2) }

  val ignored = arms.flatMap { arm -> arm.flatMap { it.neighbors } }.plus(origin.neighbors)
  val filteredGroup = group.minus(ignored.map { it.id })
  val offsets = chains.map { it.size - 1 }.toMutableList()
  var turn = chains.indexOfFirst { it.any() }

  val intersects = { o: List<Int> ->
    val points = chains.zip(o) { a, b -> a[b] }
    intersects(faces, filteredGroup, points)
  }

  while (offsets.sum() > 0) {
    if (!intersects(offsets))
      return offsets.toList()

    --offsets[turn]
    if (offsets[1 - turn] > 0)
      turn = 1 - turn
  }

  // The way turns start and alternate its possible that the only non-occluded option was skipped
  if (chains[1 - turn].size > 1) {
    val offsets2 = (0..1).map { if (it == turn) 0 else 1 }
    if (!intersects(offsets2))
      return offsets2
  }

  return null
}

fun isALoop(arms: List<List<ImmutableFace>>): Boolean {
  if (arms.all { it.none() })
    return false

  val arms2 = arms.zip(arms.reversed()) { arm, other -> if (arm.any()) arm else other }
  return arms2[0].first().id == arms2[1].last().id
}

fun gatherNewSectorFaces(faces: ImmutableFaceTable, group: List<Id>, connections: ConnectionTable, origin: ImmutableFace): List<ImmutableFace>? {
  val firstNeighbors = getIncompleteNeighbors(connections, origin).filter { !isConcaveCorner(origin, it) }.toList()
  if (firstNeighbors.none())
    return null

  val bufferedNeighbors = (0..1).map { firstNeighbors.getOrNull(it) }
  val arms = bufferedNeighbors.map {
    if (it != null)
      traceIncompleteWalls(connections, origin, it, origin)
    else
      listOf()
  }

  if (!isALoop(arms)) {
    val offsets = shaveOccluded(faces, group, origin, arms)
    return if (offsets == null)
      null
    else
      arms[0].take(offsets[0]).reversed().plus(origin).plus(arms[1].take(offsets[1]))
  }

  return arms[0].reversed().plus(origin).plus(arms[1])
}

/*
fun gatherNewSectorFaces(faces: ImmutableFaceTable, group: List<Id>, connections: ConnectionTable, origin: ImmutableFace): List<ImmutableFace>? {
  val firstNeighbors = getIncompleteNeighbors(connections, origin).filter { !isConcaveCorner(origin, it) }.toList()
  if (firstNeighbors.size != 2) {
    if (firstNeighbors.none())
      return null

    val other = firstNeighbors.first()
    val simpleResult = listOf(origin, other)
    val floor = getFloor(origin).edge
    val otherFloor = getFloor(other).edge
    val filteredGroup = group.minus(simpleResult.plus(origin.neighbors).plus(other.neighbors).map { it.id })
    return if (intersects(faces, filteredGroup, getEndPoint(floor, otherFloor), getEndPoint(otherFloor, floor)))
      null
    else
      simpleResult
  }
  assert(firstNeighbors.size == 2)

  val (firstDir, firstNotUsed) = traceIncompleteWalls(connections, origin, firstNeighbors[0], origin)
  val (secondDir, secondNotUsed) = traceIncompleteWalls(connections, origin, firstNeighbors[1], firstDir.last())
  val notUsed = if (firstNotUsed.none())
    listOf()
  else
    firstNotUsed.plus(secondNotUsed).distinct()

  return if (notUsed.any()) {
    val filteredGroup = group
        .minus(firstDir.plus(origin).plus(secondDir).plus(notUsed).map { it.id })
    val firstEdges = listOf(origin).plus(firstDir).map { getFloor(it).edge }
    val secondEdges = listOf(origin).plus(secondDir).map { getFloor(it).edge }
    val secondEndPoint = getEndPoint(secondEdges.dropLast(1).lastOrNull()
        ?: getFloor(origin).edge, secondEdges.last())
    val firstCount = shaveOccluded(faces, filteredGroup, firstEdges, secondEndPoint) - 1
    val updatedFirstDir = firstDir.take(firstCount)
    if (updatedFirstDir.none())
      return null

    val updatedFirstEdges = updatedFirstDir.map { getFloor(it).edge }
    val firstEndPoint = getEndPoint(updatedFirstEdges.dropLast(1).lastOrNull()
        ?: getFloor(origin).edge, updatedFirstEdges.last())
    val secondCount = shaveOccluded(faces, filteredGroup, secondEdges, firstEndPoint) - 1
    val updatedSecondDir = secondDir.take(secondCount)
    updatedFirstDir.reversed().plus(origin).plus(updatedSecondDir)
  } else {
    firstDir.reversed().plus(origin).plus(secondDir)
  }
}
*/
fun getDistinctEdges(edges: Edges) =
    edges.distinctBy { it.vertices.map { it.hashCode() }.sorted() }

fun createSpaceNode(sectorCenter: Vector3, nextId: IdSource): Node {
  return createSecondaryNode(sectorCenter, nextId, true, Biome.void)
}

fun addSpaceNode(idSources: StructureIdSources, realm: StructureRealm, walls: List<ImmutableFace>): Triple<Node, ConnectionTable, ImmutableFaceTable> {
  val a = getEndEdgeReversed(walls, 0)
  val b = getEndEdge(walls, 0)

//  if (walls.map { it.id }.contains(1090L)) {
//    val k = 0
//  }
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
  node.walls.addAll(walls.map { it.id })
  val updatedWalls = walls.map {
    val info = realm.connections[it.id]!!
    info.copy(secondNode = node.id)
  }

  val floor = createFloor(idSources, realm.mesh, node, floorVertices, flatCenter)
  val ceiling = createCeiling(idSources, realm.mesh, node, ceilingVertices, flatCenter)

  val gapWall = if (a != b) {
    val gapVertices = getNewWallVertices(sectorCenter, listOf(a, b))
    val facingVertices = if (!node.isSolid)
      flipVertices(gapVertices)
    else
      gapVertices

    val newWall = realm.mesh.createStitchedFace(idSources.edge, idSources.face(), facingVertices)
    println(newWall.id)

    node.walls.add(newWall.id)
    val connection = ConnectionFace(newWall.id, FaceType.wall, node.id, voidNodeId, null)
    listOf(FacePair(connection, newWall))
  } else
    listOf()

  val (newConnections, newFaces) = splitFacePairTables(
      listOf(floor, ceiling)
          .plus(gapWall)
  )

  val updatedConnections = newConnections
      .plus(entityMap(updatedWalls))

  return Triple(
      node,
      updatedConnections,
      newFaces
  )
}

fun getIncomplete(faces: ConnectionTable, nodes: Collection<Node>): List<Id> =
    nodes.flatMap { it.walls }
        .filter { faceNodeCount(faces[it]!!) == 1 }

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
  node.walls.add(originFace.id)
  val updatedOriginFace = realm.connections[originFace.id]!!.copy(secondNode = node.id)
  val outerWall = createWall(idSources, realm.mesh, node, newPoints)
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
      val missingWalls = neighborWalls.filter { !node.walls.contains(it.id) && it.edges.any { originFace.edges.map { it.edge }.contains(it.edge) } }
      node.walls.addAll(missingWalls.map { it.id })
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
//    realm.connections[face.id]!!.debugInfo = "space-a"
    val result = createBoundarySector(idSources, currentRealm, realm.mesh.faces[face]!!, dice)
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

fun getLoop(faces: ImmutableFaceTable, start: Id, available: Collection<Id>): List<Id> {
  val result: MutableList<Id> = mutableListOf(start)
  var current = start
  do {
    current = faces[current]!!.neighbors.first { it.id != current && available.contains(it.id) }.id
    result.add(current)
  } while (current != start)
  return result
}

fun groupIncompleteFaces(faces: ImmutableFaceTable, incomplete: Collection<Id>): List<List<Id>> {
  var available = incomplete
  val result: MutableList<List<Id>> = mutableListOf()
  while (available.any()) {
    val start = available.first()
    val strip: List<Id> = getLoop(faces, start, available)
    result.add(strip)
    available = available.minus(strip)
  }
  return result
}

fun fillIncompleteGroup(realm: StructureRealm, incomplete: List<Id>, idSources: StructureIdSources): StructureRealm {
  var pass = 1
  var currentRealm = realm
  var remaining = incomplete
  while (remaining.any()) {
//    val neighborLists = remaining.map { wall -> Pair(wall, getIncompleteNeighbors(currentRealm.connections, wall).toList()) }
//    val invalid = neighborLists.filter { it.second.size > 2 }
////    assert(invalid.none())
//    if (invalid.any()) {
//      val temp = invalid.map { (a, b) ->
//        realm.connections[a.id]!!.debugInfo = "space-a"
//        Pair(realm.connections[a.id]!!, b.map { realm.connections[it.id]!! })
//      }
//      val nd = temp.flatMap { (a, b) -> b.map { it.firstNode }.plus(a.firstNode) }
//          .distinct()
//      return currentRealm
//    }

//    assert(invalid.none())

    // Gather concave faces and sort them by most concave angles first
    val concaveFaces = remaining
        .mapNotNull { id ->
          if (id == 507L || id == 514L) {
            val k = 0
          }
          val wall = currentRealm.mesh.faces[id]!!
          val neighbors = getIncompleteNeighbors(currentRealm.connections, wall).toList()
          if (neighbors.size > 1) { // Currently this should always be true but just in case surrounding code changes...
            val dots = neighbors.map { edgeDot(wall, it) }
            if (dots.any { it < 0f }) { // Any are concave
              // If a wall has one convex neighbor it will incidentally be sorted near the end of the list
              val priority = -(dots.sorted().last())
              Pair(id, priority)
            } else {
              null
            }
          } else {
            null
          }
        }
        .sortedByDescending { it.second }
        .map { it.first }

    if (concaveFaces.none()) {
//      throw Error("Should not be here")
      return currentRealm.copy(
          connections = currentRealm.connections.mapValues {
            if (remaining.contains(it.key))
              it.value.copy(
                  debugInfo = "incomplete"
              )
            else
              it.value
          }
      )
    }

    val previousRealm = currentRealm

    var newConnections: List<Id> = listOf()
    for (id in concaveFaces) {
      val originFace = currentRealm.mesh.faces[id]!!
      if (faceNodeCount(currentRealm.connections, originFace) == 1) {
        if (id == 1186L) {
          val k = 0
        }
        val walls = gatherNewSectorFaces(currentRealm.mesh.faces, remaining, currentRealm.connections, originFace)
        if (walls == null)
          continue

        if (walls.size < 2) {
          // Getting here means there is a bug but it is hard to debug without a map display so
          // let execution pass through instead of throwing an exception.
          println("Negative space generation error.  Face id = " + originFace.id)
//          gatherNewSectorFaces(currentRealm.connections, originFace)
          return currentRealm
        }
        val (newNode, updatedConnections, newFaces) = addSpaceNode(idSources, currentRealm, walls)
        currentRealm = currentRealm.copy(
            nodes = currentRealm.nodes.plus(Pair(newNode.id, newNode)),
            connections = currentRealm.connections.plus(updatedConnections),
            mesh = currentRealm.mesh.copy(
                faces = currentRealm.mesh.faces.plus(newFaces)
            )
        )
        newConnections = newConnections.plus(updatedConnections.keys)
      }
    }

    if (currentRealm === previousRealm)
      return currentRealm

    remaining = remaining
        .plus(newConnections)
        .filter(isIncompleteWall(currentRealm.connections))

    ++pass
  }

  return currentRealm
}

fun defineNegativeSpace(idSources: StructureIdSources, realm: StructureRealm, dice: Dice): StructureRealm {
  val incomplete = realm.connections.filterKeys(isIncompleteWall(realm.connections)).keys
  val groups = groupIncompleteFaces(realm.mesh.faces, incomplete)
  var currentRealm = realm
//      .copy(
//      connections = realm.connections.mapValues {
//        if (incomplete.contains(it.key))
//          it.value.copy(
//              debugInfo = "incomplete"
//          )
//        else
//          it.value
//      }
//  )
  for (group in groups) {
    currentRealm = fillIncompleteGroup(currentRealm, group, idSources)
  }
  return currentRealm
}