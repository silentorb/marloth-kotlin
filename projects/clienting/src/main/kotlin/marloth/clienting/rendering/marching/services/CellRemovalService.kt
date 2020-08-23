package marloth.clienting.rendering.marching.services

import marloth.clienting.rendering.marching.CellTimingMap
import marloth.clienting.rendering.marching.MarchingState
import silentorb.mythic.spatial.Vector3i
import simulation.updating.nanosecondsInSecond

fun markCellsForRemoval(now: Long, lastUsed: CellTimingMap): Set<Vector3i> {
  val floor = now - nanosecondsInSecond * 2
  return lastUsed
      .filter { it.value < floor }
      .keys
}

fun cellRemovalService(now: Long, marching: MarchingState): MarchingState {
  val marked = markCellsForRemoval(now, marching.lastUsed)
  return if (marked.any()) {
    println("Removing ${marked.size}")
    for (key in marked) {
      marching.gpu.meshes[key]!!.vertexBuffer.dispose()
    }
    return marching.copy(
        gpu = marching.gpu.copy(
            meshes = marching.gpu.meshes - marked
        ),
        lastUsed = marching.lastUsed - marked
    )
  } else
    marching
}
