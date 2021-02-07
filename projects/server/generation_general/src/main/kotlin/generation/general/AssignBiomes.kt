package generation.general

import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.nearestFast
import silentorb.mythic.spatial.toVector3
import simulation.misc.cellHalfLength
import simulation.misc.cellLength

typealias BiomeGrid = (Vector3i) -> String

fun getGridBounds(grid: Set<Vector3i>): WorldBoundary =
    if (grid.none())
      WorldBoundary(Vector3.zero, Vector3.zero)
    else
      WorldBoundary(
          Vector3i(
              grid.minOf { it.x },
              grid.minOf { it.y },
              grid.minOf { it.z },
          ).toVector3() - cellHalfLength,
          Vector3i(
              grid.maxOf { it.x },
              grid.maxOf { it.y },
              grid.maxOf { it.z },
          ).toVector3() + cellHalfLength
      )

fun <T> newVoronoiGrid(biomes: List<T>, dice: Dice, bounds: WorldBoundary): VoronoiAnchors<T> {
  val gridScale = 0.5f
  val dimensions = bounds.dimensions * gridScale
  val width = dimensions.x.toInt()
  val height = dimensions.y.toInt()
  val anchorCount = (width * height * 0.1f).toInt()
  val values = biomes.toList()
  return voronoiAnchors(values, anchorCount, dice, bounds.start, bounds.end)
}

fun newBiomeAnchors(biomes: Set<String>, dice: Dice, cellCount: Int): VoronoiAnchors<String> {
  val padding = 1
  val length = (cellCount + padding).toFloat()
  val bounds = WorldBoundary(
      Vector3(-length, -length, -length),
      Vector3(length, length, length)
  )
  return newVoronoiGrid(biomes.toList(), dice, bounds)
}

fun biomeGridFromAnchors(anchors: VoronoiAnchors<String>): BiomeGrid {
  val items = anchors.map { Pair(it.position, it.value) }
  val nearest = nearestFast(items)
  return { cell ->
    nearest(cell.toVector3())
  }
}

//private fun logGrid(grid: Grid<BiomeId>, boundary: WorldBoundary) {
//  for (y in 0 until (boundary.dimensions.y).toInt()) {
//    val line = (0 until (boundary.dimensions.x).toInt()).map { x ->
//      grid(
//          x.toFloat() - boundary.dimensions.x / 2f,
//          y.toFloat() - boundary.dimensions.y / 2f)
//          .ordinal
//    }
//        .joinToString(" ")
//    println(line)
//  }
//}

