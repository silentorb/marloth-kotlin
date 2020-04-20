package generation.architecture.misc

import silentorb.mythic.spatial.Vector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import simulation.misc.Realm

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)

fun generateRealm(grid: MapGrid, cellBiomes: CellBiomeMap): Realm {
  return Realm(
      cellBiomes = cellBiomes,
      nodeList = listOf(),
      grid = grid
  )
}
