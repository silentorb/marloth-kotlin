package generation.general

import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.*
import simulation.misc.cellHalfLength

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
  val averageBiomeLength = 20
  val lengthMod = averageBiomeLength * averageBiomeLength
  val dimensions = bounds.dimensions// * gridScale
  val x = dimensions.x.toInt()
  val y = dimensions.y.toInt()
  val anchorCount = x * y / lengthMod
  val values = biomes.toList()
  return voronoiAnchors(values, anchorCount, dice, bounds.start, bounds.end)
}

fun newBiomeAnchors(biomes: Set<String>, dice: Dice, cellCount: Int): VoronoiAnchors2d<String> {
  val padding = 1
  val length = (cellCount / 2).toFloat()
  val bounds = WorldBoundary(
      Vector3(-length, -length, -length),
      Vector3(length, length, length)
  )
  return newVoronoiGrid(biomes.toList(), dice, bounds).map { VoronoiAnchor2d(it.position.xy(), it.value) }
}

fun biomeGridFromAnchors(anchors: VoronoiAnchors2d<String>): BiomeGrid {
  val items = anchors.map { Pair(it.position, it.value) }
  val nearest = nearestFast2d(items)
  return { cell ->
    nearest(cell.toVector3().xy())
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

