package generation.general

import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.*
import simulation.misc.cellHalfLength
import kotlin.math.max

typealias BiomeGrid = (Vector3i) -> String
typealias VoronoiAnchors2d<T> = List<VoronoiAnchor2d<T>>

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

tailrec fun removeClumps(range: Float, nodes: VoronoiAnchors2d<String>, removed: VoronoiAnchors2d<String> = listOf()): VoronoiAnchors2d<String> =
    if (nodes.none())
      removed
    else {
      val next = nodes.first()
      val remaining = nodes.drop(1)
      val tooClose = remaining
          .filter { it.position.distance(next.position) < range }

      removeClumps(range, remaining - tooClose, removed + tooClose)
    }

fun <T> newVoronoiGrid(biomes: List<T>, dice: Dice, bounds: WorldBoundary): VoronoiAnchors<T> {
  val averageBiomeLength = 20
  val lengthMod = averageBiomeLength * averageBiomeLength
  val dimensions = bounds.dimensions// * gridScale
  val x = dimensions.x.toInt()
  val y = dimensions.y.toInt()
  val anchorCount = max(1, x * y / lengthMod)
  val values = biomes.toList()
  return voronoiAnchors(values, anchorCount, dice, bounds.start, bounds.end)
}

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

