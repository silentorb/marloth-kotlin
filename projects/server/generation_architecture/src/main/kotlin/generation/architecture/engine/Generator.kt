package generation.architecture.engine

import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import simulation.misc.Realm

fun generateRealm(grid: MapGrid, cellBiomes: CellBiomeMap): Realm {
  return Realm(
      cellBiomes = cellBiomes,
      nodeList = listOf(),
      grid = grid
  )
}
