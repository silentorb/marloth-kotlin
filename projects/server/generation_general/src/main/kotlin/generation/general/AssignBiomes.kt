package generation.general

import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.*
import simulation.misc.cellHalfLength
import kotlin.math.max

typealias BiomeGrid = (Vector3i) -> String
typealias VoronoiAnchors2d<T> = List<VoronoiAnchor2d<T>>

fun newBiomeAnchors(biomes: Set<String>, dice: Dice, worldRadius: Float, biomeSize: Float, minGap: Float): VoronoiAnchors2d<String> {
  val biomeLengthCount = max(1, (worldRadius * 2f / biomeSize).toInt())
  val margin = minGap / 2f
  val randomRange = biomeSize - minGap
  val startOffset = -biomeSize * biomeLengthCount / 2f
  val anchors = (0 until biomeLengthCount).flatMap { y ->
    (0 until biomeLengthCount).map { x ->
      VoronoiAnchor2d(
          position = Vector2(
              startOffset + x * biomeSize + margin + dice.getFloat(0f, randomRange),
              startOffset + y * biomeSize + margin + dice.getFloat(0f, randomRange),
          ),
          value = dice.takeOne(biomes)
      )
    }
  }
  return anchors
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

