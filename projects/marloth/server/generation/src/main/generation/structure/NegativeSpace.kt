package generation.structure

import generation.BiomeId
import mythic.ent.*
import mythic.sculpting.*
import mythic.spatial.*
import simulation.physics.voidNodeId
import randomly.Dice
import simulation.misc.*

fun isIncompleteWall(faces: ConnectionTable): (Id) -> Boolean = { id ->
  val info = faces[id]!!
  val c = faceNodeCount(info) == 1
  info.faceType == FaceType.wall && c
}

fun getIncompleteNeighbors(faces: ConnectionTable, face: ImmutableFace): Collection<ImmutableFace> =
    face.neighbors
        .filter { isIncompleteWall(faces)(it.id) }

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

val lineChainToVertexChain: (List<ImmutableEdge>) -> List<Vector3> = { edges ->
  listOf(edges[0].vertices.first { !edges[1].vertices.contains(it) })
      .plus(edges.drop(1).zip(edges.dropLast(1)) { c, d -> c.vertices.intersect(d.vertices).first() })
      .plus(edges.last().vertices.first { !edges[edges.size - 2].vertices.contains(it) })
}

fun intersects(faces: ImmutableFaceTable, group: Collection<Id>, points: List<Vector3>): Boolean =
    group.any { id ->
      if (id == 436L) {
        val k = 0
      }
      val face = faces[id]!!
      val floor = getFloor(face)
      if (points.any { floor.vertices.contains(it) }) {
        false
      } else {
        val (hit, _) = lineSegmentIntersectsLineSegment(points[0], points[1], floor.first, floor.second)
        if (hit) {
          val k = 0
//          println("Hit " + id)
        }
        hit
      }
    }

fun shaveOccluded(faces: ImmutableFaceTable, group: Set<Id>, origin: ImmutableFace, arms: List<List<ImmutableFace>>): List<Int>? {
  if (arms.all { it.isEmpty() })
    return null

  val originFloor = getFloor(origin).edge
  val edgeArms = arms.map { arm -> arm.map { getFloor(it).edge } }

  val chains = edgeArms
      .zip(edgeArms.reversed()) { a, b ->
        if (a.isEmpty())
          lineChainToVertexChain(listOf(b.first(), originFloor)).drop(2)
        else
          lineChainToVertexChain(listOf(originFloor).plus(a)).drop(1)
      }

  val offsets = chains.map { it.size - 1 }.toMutableList()
  var turn = chains.indexOfFirst { it.any() }

  while (offsets.sum() > 0) {
    val points = chains.zip(offsets) { a, b -> a[b] }
    if (points[0] != points[1] && !intersects(faces, group, points)) {
      val ends = arms.zip(offsets) { it, offset -> it.getOrElse(offset - 1) { origin } }
      if (ends.all { end -> edgeDot(points, getFloor(end).vertices, end.normal) < 0f })
        return offsets.toList() //.mapIndexed { i, it -> arms[i].size - chains[i].size + 1 + it }
    }
    --offsets[turn]
    if (offsets[turn] == 0)
//    if (offsets[1 - turn] > 0)
      turn = 1 - turn
  }

  // The way turns start and alternate its possible that the only non-occluded option was skipped
  if (chains[1 - turn].size > 1) {
    val offsets2 = (0..1).map { if (it == turn) 0 else 1 }
    val points = chains.zip(offsets2) { a, b -> a[b] }
    if (!intersects(faces, group, points))
      return offsets2
  }

  return null
}

fun isALoop(arms: List<List<ImmutableFace>>): Boolean {
  if (arms.any { it.none() } || arms.sumBy { it.size } < 2)
    return false

  return arms[0].first().id == arms[1].last().id
}

fun gatherNewSectorFaces(connections: ConnectionTable, origin: ImmutableFace): List<List<ImmutableFace>>? {
  val firstNeighbors = getIncompleteNeighbors(connections, origin).filter { !isConcaveCorner(origin, it) }.toList()
  if (firstNeighbors.none())
    return null

  val bufferedNeighbors = (0..1).map { firstNeighbors.getOrNull(it) }
  return bufferedNeighbors.map {
    if (it != null)
      traceIncompleteWalls(connections, origin, it, origin)
    else
      listOf()
  }
}

fun prepareNewSectorFaces(faces: ImmutableFaceTable, group: Set<Id>, arms: List<List<ImmutableFace>>, origin: ImmutableFace): List<ImmutableFace>? {
  if (!isALoop(arms)) {
    val offsets = shaveOccluded(faces, group, origin, arms)
    return if (offsets == null)
      null
    else
      arms[0].take(offsets[0]).reversed().plus(origin).plus(arms[1].take(offsets[1]))
  }

  return listOf(origin).plus(arms[1])
}

fun getDistinctEdges(edges: Edges) =
    edges.distinctBy { it.vertices.map { it.hashCode() }.sorted() }

fun createSpaceNode(sectorCenter: Vector3, nextId: IdSource): Node {
  return createSecondaryNode(sectorCenter, nextId, true, BiomeId.void)
}

fun newSpaceNode(idSources: StructureIdSources, realm: StructureRealm, walls: List<ImmutableFace>): Triple<Node, ConnectionTable, ImmutableFaceTable> {
  val a = getEndEdgeReversed(walls, 0)
  val b = getEndEdge(walls, 0)

  assert(walls.size > 2 || (walls.size > 1 && a != b))

  val floorVertices = walls
      .flatMap { getFloor(it).vertices }
      .distinct()

  val ceilingVertices = walls
      .flatMap { getCeiling(it).vertices }
      .distinct()

  assert(hasNoDuplicates(floorVertices))
  assert(hasNoDuplicates(ceilingVertices))

  val sectorCenter = getCenter(floorVertices.plus(ceilingVertices))
  val flatCenter = sectorCenter.xy()

  val node = createSpaceNode(sectorCenter, idSources.node)
  node.walls.addAll(walls.map { it.id })
  val updatedWalls = walls.map {
    val info = realm.connections[it.id]!!
    info.copy(secondNode = node.id)
  }

  val floor = createFloor(idSources, realm.mesh, node, floorVertices, flatCenter)
  val ceiling = createCeiling(idSources, realm.mesh, node, ceilingVertices, flatCenter)

  // Temporary assertions
  isFacingOutward(ceiling.geometry, node.position)
  isFacingOutward(floor.geometry, node.position)

  val gapWall = if (a.id == b.id || a.faces.any { it.id == b.id }) {
    listOf()
  } else {
    val gapVertices = getNewWallVertices(sectorCenter, listOf(a, b))
    val facingVertices = if (!node.isSolid)
      flipVertices(gapVertices)
    else
      gapVertices

    val newWall = realm.mesh.createStitchedFace(idSources.edge, idSources.face(), facingVertices)
//    println(newWall.id)

    node.walls.add(newWall.id)
    val connection = ConnectionFace(newWall.id, FaceType.wall, node.id, voidNodeId, null)
    listOf(FacePair(connection, newWall))
  }

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
//  val originalWall = getWallVertices(originFace.vertices)

  val newPoints = originFace.vertices.map {
    val projected = it.xy() + it.xy().normalize() * 10f
    Vector3(projected.x, projected.y, it.z)
  }
  val getOriginalEdge = { newEdge: ImmutableEdgeReference ->
    val indexes = newEdge.vertices.map { newPoints.indexOf(it) }
    val originalPoints = indexes.map { originFace.vertices[it] }
    originFace.edge(originalPoints[0], originalPoints[1])!!
  }

  val sectorCenter = getCenter(originFace.vertices.plus(newPoints))
  val node = createSpaceNode(sectorCenter, idSources.node)
  val outerWall = createWall(idSources, realm.mesh, node, newPoints)
  val floorVertices = getFloor(originFace).vertices.plus(getFloor(outerWall.geometry).vertices)
  val ceilingVertices = getCeiling(originFace).vertices.plus(getCeiling(outerWall.geometry).vertices)

  val floor = createFloor(idSources, realm.mesh, node, floorVertices, sectorCenter.xy())
  val ceiling = createCeiling(idSources, realm.mesh, node, ceilingVertices, sectorCenter.xy())

  // Temporary assertions
  isFacingOutward(outerWall.geometry, node.position)
  isFacingOutward(ceiling.geometry, node.position)
  isFacingOutward(floor.geometry, node.position)

  node.walls.add(originFace.id)
  val updatedOriginFace = realm.connections[originFace.id]!!.copy(secondNode = node.id)
  val faces1 = listOf(outerWall)
      .plus(floor)
      .plus(ceiling)
      .plus(FacePair(updatedOriginFace, originFace))

  val faceTable = realm.connections.plus(entityMap(faces1.map { it.info }))
  val missingWallsAccumulator = mutableListOf<FacePair>()
  val sideEdges = outerWall.geometry.edges.filter(isVerticalEdgeLimited)
  assert(sideEdges.size == 2)
  val sideWalls = sideEdges.filter { sideEdge ->
    val neighborWalls = sideEdge.faces.filter { faceTable[it.id]!!.faceType == FaceType.wall }
    if (neighborWalls.size > 1) {
      val missingWalls = neighborWalls.filter { !node.walls.contains(it.id) && it.edges.any { originFace.edges.map { it.edge }.contains(it.edge) } }
      node.walls.addAll(missingWalls.map { it.id })
      missingWallsAccumulator.plus(missingWalls.associate { Pair(it.id, faceTable[it.id]!!.copy(secondNode = node.id)) })

      false
    } else
      true
  }.map { sideEdge ->
    val originalSideEdge = getOriginalEdge(sideEdge)
    val sidePoints = originalSideEdge.vertices
        .sortedBy { it.z }
        .plus(sideEdge.vertices.sortedByDescending { it.z })
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

fun getLoop(faces: ImmutableFaceTable, start: Id, available: Collection<Id>): Set<Id> {
  val result: MutableSet<Id> = mutableSetOf(start)
  var current = start
  do {
    current = faces[current]!!.neighbors.first { it.id != current && available.contains(it.id) }.id
    assert(!result.contains(current) || current == start)
    result.add(current)
  } while (current != start)
  return result
}

fun groupIncompleteFaces(faces: ImmutableFaceTable, incomplete: Collection<Id>): List<Set<Id>> {
  var available = incomplete
  val result: MutableList<Set<Id>> = mutableListOf()
  while (available.any()) {
    val start = available.first()
    val strip: Set<Id> = getLoop(faces, start, available)
    result.add(strip)
    available = available.minus(strip)
  }
  return result
}

/* This function currently has some redundant checks and could be optimized with additional code */
fun shapeEvenness(edges: List<ImmutableEdge>): Float {
  val points = edges.flatMap { it.vertices }.distinct()
  val lengths = points.map { p -> points.map { it.distance(p) }.sortedDescending().first() }
      .sorted()

  return lengths.first() / lengths.last()
}

fun fillIncompleteGroup(realm: StructureRealm, incomplete: Set<Id>, idSources: StructureIdSources): StructureRealm {
  var pass = 1
  var currentRealm = realm
  var remaining = incomplete
  while (remaining.any()) {
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

    var newConnections: Set<Id> = setOf()
    val sectorBundles = concaveFaces.mapNotNull { (id, priority) ->
      val originFace = currentRealm.mesh.faces[id]!!
      val arms = gatherNewSectorFaces(currentRealm.connections, originFace)
      if (arms == null)
        null
      else
        Triple(
            originFace,
            arms,
//            shapeEvenness(arms.flatMap { arm -> arm.map { getFloor(it).edge } }) + priority
//                priority
            shapeEvenness(arms.flatMap { arm -> arm.map { getFloor(it).edge } })
        )
    }
        .sortedByDescending { it.third }

    // In the majority of cases only the remaining set needs to be checked for occlusion,
    // but there are rare cases where they can still be hit and need to be checked against.
    val occludingWalls = remaining.plus(incomplete)

//    if (originFace.id == 533L) {
//      val k = 0
//    }

//    val walls = prepareNewSectorFaces(currentRealm.mesh.faces, occludingWalls, arms, originFace)
    val walls = sectorBundles.firstNotNull { (originFace, arms) ->
      prepareNewSectorFaces(currentRealm.mesh.faces, occludingWalls, arms, originFace)
    }
    if (walls != null) {
      val (newNode, updatedConnections, newFaces) = newSpaceNode(idSources, currentRealm, walls)
      currentRealm = currentRealm.copy(
          nodes = currentRealm.nodes.plus(Pair(newNode.id, newNode)),
          connections = currentRealm.connections.plus(updatedConnections),
          mesh = currentRealm.mesh.copy(
              faces = currentRealm.mesh.faces.plus(newFaces)
          )
      )
      if (newNode.id == 289L) {
        val k = 0
      }
      newConnections = newConnections.plus(updatedConnections.keys)
    } else {
      throw Error("Could not find a valid wall chain.")
    }

    if (currentRealm === previousRealm)
      return currentRealm

    remaining = remaining
        .plus(newConnections)
        .filter(isIncompleteWall(currentRealm.connections)).toSet()

    ++pass
  }

  return currentRealm
}

fun defineNegativeSpace(idSources: StructureIdSources, realm: StructureRealm, dice: Dice): StructureRealm {
  val incomplete = realm.connections.filterKeys(isIncompleteWall(realm.connections)).keys
  val groups = groupIncompleteFaces(realm.mesh.faces, incomplete)
  var currentRealm = realm

  for (group in groups) {
    currentRealm = fillIncompleteGroup(currentRealm, group, idSources)
  }
  return currentRealm
}
