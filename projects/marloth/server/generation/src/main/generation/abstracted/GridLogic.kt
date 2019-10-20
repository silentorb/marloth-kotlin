package generation.abstracted

import mythic.spatial.Vector3i
import simulation.misc.MapGrid

fun isCellOpen(grid: MapGrid, from: Vector3i, connectionOffset: Vector3i): Boolean {
  val lower = from + connectionOffset * 2
  val upper = lower + Vector3i(0, 0, 1)
  return !grid.cells.containsKey(lower) && !grid.cells.containsKey(upper)
}
