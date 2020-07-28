package generation.architecture.engine

import simulation.misc.MapGrid
import simulation.misc.Realm

fun generateRealm(grid: MapGrid): Realm {
  return Realm(
      grid = grid
  )
}
