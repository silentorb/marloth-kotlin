package generation.structure

import mythic.sculpting.*
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.xy
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

fun gatherNewSectorFaces(origin: FlexibleFace): List<FlexibleFace> {
  val result = mutableListOf(origin)
  var recent = mutableListOf(origin)
  while (recent.any()) {
    val next = recent
    recent = mutableListOf()
    for (face in next) {
      val neighbors = getIncompleteNeighbors(face)
      for (neighbor in neighbors) {
        if (!result.contains(neighbor) && !isConcaveCorner(face, neighbor)) {
          result.add(neighbor)
          recent.add(neighbor)
        }
      }
    }
  }
  return result
}

//fun processIncompleteEdges(edges: List<FlexibleFace>) {
//  val incompleteEdges = edges.filter { faceNodeCount(getFaceInfo(it)) == 1 }
//
//  val acuteCorners = incompleteEdges.asSequence().filter { getConcaveCorners(it).any() }
//
//  val cornerFace = acuteCorners.firstOrNull()
//  if (cornerFace == null)
//    return
//
//  addSpaceNode(cornerFace)
//}

fun getNewWallVertices(sectorCenter: Vector3, edges: Edges): Vertices {
  val sortedEdges = edges.map { it.vertices.sortedBy { it.z } }
  val vertices = listOf(
      sortedEdges[0][0],
      sortedEdges[1][0],
      sortedEdges[1][1],
      sortedEdges[0][1]
  )
  val normal = getNormal(vertices)
  val center = getCenter(vertices)
  if (sectorCenter.distance(center + normal) > sectorCenter.distance(center - normal)) {
    return listOf(
        sortedEdges[1][0],
        sortedEdges[0][0],
        sortedEdges[0][1],
        sortedEdges[1][1]
    )
  }

  return vertices
}

fun getDistinctEdges(edges: Edges) =
    edges.distinctBy { it.vertices.map { it.hashCode() }.sorted() }

fun addSpaceNode(abstractWorld: AbstractWorld, originFace: FlexibleFace) {
  val walls = gatherNewSectorFaces(originFace)
  assert(walls.size > 2)
//  val duplicatedEdges = walls.flatMap { face ->
//    face.edges.filter { edge ->
//      edge.faces.all { getFaceInfo(it).type == FaceType.wall }
//          && edge.otherEdgeReferences.any()
//    }
//  }
//  val edges = getDistinctEdges(duplicatedEdges)
  val edges = walls.flatMap { face ->
    face.edges.filter { edge ->
      edge.first.z != edge.second.z
    }.map { it.edge }
  }.distinct()

  val floorVertices = edges.map { it.vertices.sortedBy { it.z }.first() }
  val sectorCenter = getCenter(floorVertices)
  val flatCenter = sectorCenter.xy
//  val sortedFloorVertices = arrangePointsCounterClockwise(sectorCenter, floorVertices)

  val sortedFloorVertices = floorVertices
      .sortedBy { atan(it.xy - flatCenter) }
  val a = sortedFloorVertices.map { atan(it.xy - flatCenter) }
  val floor = abstractWorld.mesh.createStitchedFace(sortedFloorVertices)
  floor.updateNormal()

//  val radius = floorVertices.map { it.distance(sectorCenter) }.average().toFloat()
  val radius = 1f
  val node = Node(
      position = sectorCenter,
      radius = radius,
      type = NodeType.space
  )
  node.index = abstractWorld.graph.nodes.size
  node.walls.addAll(walls)
  node.floors.add(floor)
  initializeFaceInfo(node)

  val gapEdges = edges.filter {
    it.faces.count { walls.contains(it) } < 2
  }
  if (gapEdges.any()) {
    assert(gapEdges.size == 2)
    val gapVertices = getNewWallVertices(sectorCenter, gapEdges)
    val newWall = abstractWorld.mesh.createStitchedFace(gapVertices)
    newWall.updateNormal()
    initializeFaceInfo(FaceType.wall, node, newWall)
    val n = getIncompleteNeighbors(newWall).toList()
    val n2 = getIncompleteNeighbors(newWall).toList()
    val n3 = newWall.neighbors
    node.walls.add(newWall)
  }

  initializeFaceInfo(node)
  abstractWorld.graph.nodes.add(node)
  node.walls.forEach {
    //    getFaceInfo(it).debugInfo = "space-c"
  }
}

fun defineNegativeSpace(abstractWorld: AbstractWorld) {
  var pass = 1
  while (true) {
    val faces = abstractWorld.graph.nodes.flatMap { it.walls }
        .filter { faceNodeCount(getFaceInfo(it)) == 1 }

    val neighborLists = faces.map { wall -> getIncompleteNeighbors(wall).toList() }
    val invalid = neighborLists.filter { it.size > 2 }
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
        if (walls.size < 3) {
          val i = getIncompleteNeighbors(originFace).toList()
//          getFaceInfo(originFace).debugInfo = "space-d"
          return
        }
        addSpaceNode(abstractWorld, originFace)
      }
    }
    ++pass
  }
}