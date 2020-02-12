package generation.architecture.misc

import generation.abstracted.gridToGraph
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import simulation.misc.Realm

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)

fun generateRealm(grid: MapGrid, cellBiomes: CellBiomeMap): Realm {
  val (graph, cellMap) = gridToGraph()(grid)

  return Realm(
      graph = graph,
      cellMap = cellMap,
      cellBiomes = cellBiomes,
      nodeList = graph.nodes.values.toList(),
      grid = grid
  )
}
