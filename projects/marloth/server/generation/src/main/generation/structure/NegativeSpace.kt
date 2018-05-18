package generation.structure

import mythic.sculpting.FlexibleFace
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

fun getAcuteAngles(face: FlexibleFace): Sequence<FlexibleFace> =
    face.neighbors
        .asSequence()
        .filter { getFaceInfo(it).type == FaceType.wall }
        .filter {
          val dot = face.normal.dot(it.normal)
          dot > 0
        }

fun addSpaceNode(face: FlexibleFace) {
  val neighbor = getAcuteAngles(face).first()
  getFaceInfo(face).debugField = "space-a"
  getFaceInfo(neighbor).debugField = "space-b"
}

fun processIncompleteEdges(edges: List<FlexibleFace>) {
  val incompleteEdges = edges.filter { faceNodeCount(getFaceInfo(it)) == 1 }

  val acuteCorners = incompleteEdges.asSequence().filter { getAcuteAngles(it).any() }

  val cornerFace = acuteCorners.firstOrNull()
  if (cornerFace == null)
    return

  addSpaceNode(cornerFace)
}

fun defineNegativeSpace(sectors: List<TempSector>) {
  val edges = sectors.flatMap { it.node.walls }
  processIncompleteEdges(edges)
}