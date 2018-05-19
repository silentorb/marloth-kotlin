package generation.structure

import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import org.joml.minus
import simulation.FaceInfo
import simulation.FaceType
import simulation.getFaceInfo

fun <T> zeroIfNull(value: T?) =
    if (value == null)
      0
    else
      1

fun faceNodeCount(faceInfo: FaceInfo) =
    zeroIfNull(faceInfo.firstNode) + zeroIfNull(faceInfo.secondNode)

//fun getNextFace(face: FlexibleFace): FlexibleFace? {
//  return face.edges.filter()
//  val info = getFaceInfo(face)
//  val node = info.firstNode!!
//  return node.walls.filter { it != face && it.vertices.union(face.vertices).size >= 2 }.firstOrNull()
//}

fun getSharedEdge(first: FlexibleFace, second: FlexibleFace): FlexibleEdge =
    first.edges.first { edge -> edge.edges.any { it.face == second } }

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

fun getConcaveCorners(face: FlexibleFace): Sequence<FlexibleFace> =
    face.neighbors
        .asSequence()
        .filter { getFaceInfo(it).type == FaceType.wall }
        .filter { isConcaveCorner(face, it) }

fun addSpaceNode(face: FlexibleFace) {
//  val neighbor = getConcaveCorners(face).first()
//  getFaceInfo(face).debugInfo = "space-a"
//  getFaceInfo(neighbor).debugInfo = "space-b"


}

fun processIncompleteEdges(edges: List<FlexibleFace>) {
  val incompleteEdges = edges.filter { faceNodeCount(getFaceInfo(it)) == 1 }

  val acuteCorners = incompleteEdges.asSequence().filter { getConcaveCorners(it).any() }

  val cornerFace = acuteCorners.firstOrNull()
  if (cornerFace == null)
    return

  addSpaceNode(cornerFace)
}

fun defineNegativeSpace(sectors: List<TempSector>) {
  val edges = sectors.flatMap { it.node.walls }
//  processIncompleteEdges(edges)
  edges
      .filter { wall -> wall.neighbors.filter { getFaceInfo(it).type == FaceType.wall }.all { isConcaveCorner(wall, it) } }
      .forEach{
        getFaceInfo(it).debugInfo = "space-a"
      }

  edges
      .filter { wall -> wall.neighbors.filter { getFaceInfo(it).type == FaceType.wall }.all { !isConcaveCorner(wall, it) } }
      .forEach{
        getFaceInfo(it).debugInfo = "space-b"
      }
}