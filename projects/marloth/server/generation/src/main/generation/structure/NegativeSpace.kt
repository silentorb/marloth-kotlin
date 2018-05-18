package generation.structure

import mythic.sculpting.FlexibleFace
import simulation.FaceNodes
import simulation.FaceSectorMap

fun <T> zeroIfNull(value: T?) =
    if (value == null)
      0
    else
      1

fun faceNodeCount(faceMap: FaceNodes) =
    zeroIfNull(faceMap.first) + zeroIfNull(faceMap.second)

fun getNextFace(face: FlexibleFace, faceMap: FaceSectorMap): FlexibleFace? {
  val node = faceMap[face]!!.first!!
  return node.walls.filter { it != face && it.vertices.union(face.vertices).size >= 2 }.firstOrNull()
}

fun processIncompleteEdges(edges: List<FlexibleFace>, faceMap: FaceSectorMap) {
  val incompleteEdges = edges.filter { faceNodeCount(faceMap[it]!!) == 1 }

  val acuteCorners = incompleteEdges.filter {
    val next = getNextFace(it, faceMap)
    if (next == null) {
      false
    } else {
      val dot = next.normal.dot(it.normal)
      dot > 0
    }
  }
}

tailrec fun defineNegativeSpace(sectors: List<TempSector>, faceMap: FaceSectorMap) {
  val edges = sectors.flatMap { it.node.walls }
  processIncompleteEdges(edges, faceMap)
}