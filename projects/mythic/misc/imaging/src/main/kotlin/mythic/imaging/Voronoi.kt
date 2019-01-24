package mythic.imaging

import mythic.spatial.Vector2
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import org.joml.Vector2i
import org.joml.plus
import randomly.Dice

data class AnchorGrid(
    val length: Int,
    val cells: List<Vector2?>
)

fun newAnchorGrid(dice: Dice, resolution: Int, roughAnchorCount: Int): AnchorGrid {

  val cellCount = resolution * resolution
  val cellLength = 1f / resolution.toFloat()
  val cellAnchorChance = roughAnchorCount.toFloat() / cellCount.toFloat()
  val cells = Array<Vector2?>(cellCount) { null }

  val minDistance = cellLength / 2f

  // Note: minDistance is Manhattan, not direct

  // Ensure that even if a cell is completely surrounded by neighbor anchors pushing against its boundaries,
  // the cell can still hold an anchor in its middle.
  // This is an extreme case to be worried about, but I've found that in generation code randomness needs
  // to be completely controlled or endless woes and surprises follow.
//  assert(minDistance < resolution.toFloat() / 2f)

  var i = 0

  val getRange = { step: Int, previous: Float?, next: Float? ->
    val start = step * cellLength
    val end = start + cellLength
    val paddedStart = if (previous != null)
      Math.max(start, previous + minDistance)
    else
      start

    val paddedEnd = if (next != null)
      Math.min(end, next - minDistance)
    else
      end

    dice.getFloat(paddedStart, paddedEnd)
  }

  val left = { j: Int ->
    if (j == 0)
      cellCount - 1
    else
      j - 1
  }

  val right = { j: Int ->
    if (j >= cellCount - 1)
      cellCount - 1
    else
      j + 1
  }

  val above = { j: Int ->
    if (j + resolution < cellCount)
      j + resolution
    else
      j + resolution - cellCount
  }

  val below = { j: Int ->
    if (j >= resolution)
      j - resolution
    else
      j + cellCount - resolution
  }

  val getX = { getter: (Int) -> Int ->
    cells[getter(i)]?.x
  }

  val getY = { getter: (Int) -> Int ->
    cells[getter(i)]?.y
  }

  for (y in 0 until resolution) {
    for (x in 0 until resolution) {
      val hasAnchor = dice.getFloat() < cellAnchorChance
      if (hasAnchor) {
        val anchor = Vector2(
            getRange(x, getX(left), getX(right)),
            getRange(y, getY(below), getY(above))
        )
        cells[i] = anchor
      }
      ++i
    }
  }
  return AnchorGrid(
      cells = cells.toList(),
      length = resolution
  )
}

fun anchorGridCell(grid: AnchorGrid, x: Int, y: Int): Vector2? {
  val cellCount = grid.length * grid.length
  val i = y * grid.length + x
  val i2 = if (i < 0)
    i + cellCount
  else if (i >= cellCount)
    i - cellCount
  else
    i

  return grid.cells[i2]
}

//private fun offsets() = (-1..1).asSequence().flatMap { y ->
//  (-1..1).asSequence().map { x -> Vector2i(x, y) }
//}

private fun offsets(step: Int): Sequence<Vector2i> {
  // Forms a boundary like:
  //
  //   ###
  //   # #
  //   ###

  val short = step - 1
  val fullRange = (-step..step).asSequence()
  val shortRange = (-short..short).asSequence()
  return fullRange.map { Vector2i(it, -step) } +
      shortRange.map { Vector2i(-step, it) } +
      shortRange.map { Vector2i(step, it) } +
      fullRange.map { Vector2i(it, step) }
}

fun getNearestCells(grid: AnchorGrid, i: Vector2i, minimumCount: Int): List<Vector2> {
  var step = 1
  var cells = listOfNotNull(anchorGridCell(grid, i.x, i.y))

  val gatherCells = { s: Int ->
    cells +
        offsets(s)
            .mapNotNull {
              val result = anchorGridCell(grid, it.x + i.x, it.y + i.y)
              if (result != null) {
//                val m = (it + i).toVector2() + result - result.toVector2i().toVector2()
                result
              } else
                null
            }.toList()
  }

  while (true) {
    cells = gatherCells(step)

    if (cells.size >= minimumCount)
      return gatherCells(step + 1)

    if (step++ >= grid.length / 2)
      throw Error("Anchor grid does not have enough cells")
  }
}

fun voronoiBoundaryHighlight(grid: AnchorGrid, thickness: Float): Sampler = { x, y ->
  val input = Vector2(x, y)
  val i = (input * grid.length.toFloat()).toVector2i()
  val offset = input - i.toVector2()
  val options = getNearestCells(grid, i, 2)
//  val options = grid.cells.filterNotNull()
  val nearestPair = options.sortedBy { it.distance(input) }.take(2)
  val gap = Math.abs(nearestPair[0].distance(input) - nearestPair[1].distance(input))
  if (anchorGridCell(grid, i.x, i.y) != null) {
    val k = 0
  }
  val overlay = if (anchorGridCell(grid, i.x, i.y) != null)
    0f
  else
    0.2f

  val result = if (gap < thickness / 2f)
    0f
  else
    0.8f

  if (nearestPair[0].distance(input) < 0.01f)
    0f
  else
    result + overlay
//  if (anchorGridCell(grid, i.x, i.y) == null)
//    0f
//  else
//    1f
}